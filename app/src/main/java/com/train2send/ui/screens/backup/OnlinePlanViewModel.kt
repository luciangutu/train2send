package com.train2send.ui.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.train2send.Train2SendApp
import com.train2send.data.model.GitHubFile
import com.train2send.data.repository.OnlinePlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class OnlinePlansUiState {
    object Loading : OnlinePlansUiState()
    data class Success(val plans: List<OnlinePlanItem>) : OnlinePlansUiState()
    data class Error(val message: String) : OnlinePlansUiState()
}

data class OnlinePlanItem(
    val file: GitHubFile,
    val status: PlanStatus
)

enum class PlanStatus {
    NEW, UPDATE, INSTALLED
}

class OnlinePlanViewModel(
    private val repository: OnlinePlanRepository
) : ViewModel() {

    private val _onlineFiles = MutableStateFlow<List<GitHubFile>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _downloadSuccess = MutableStateFlow<String?>(null)

    val downloadSuccess: StateFlow<String?> = _downloadSuccess.asStateFlow()

    val uiState: StateFlow<OnlinePlansUiState> = combine(
        _onlineFiles,
        _isLoading,
        _errorMessage,
        repository.installedPlanShas
    ) { files, loading, error, installedShas ->
        when {
            loading -> OnlinePlansUiState.Loading
            error != null -> OnlinePlansUiState.Error(error)
            else -> {
                val items = files.map { file ->
                    val localSha = installedShas[file.name]
                    val status = when {
                        localSha == null -> PlanStatus.NEW
                        localSha != file.sha -> PlanStatus.UPDATE
                        else -> PlanStatus.INSTALLED
                    }
                    OnlinePlanItem(file, status)
                }
                OnlinePlansUiState.Success(items)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = OnlinePlansUiState.Loading
    )

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val files = repository.fetchOnlinePlans()
                if (files.isEmpty()) {
                    _errorMessage.value = "No plans found or network error."
                } else {
                    _onlineFiles.value = files
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadPlan(item: OnlinePlanItem) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.downloadAndImportPlan(item.file)
                .onSuccess {
                    _downloadSuccess.value = "Plan \"${item.file.name.removeSuffix(".json")}\" imported successfully!"
                }
                .onFailure { e ->
                    _errorMessage.value = "Download failed: ${e.message}"
                }
            _isLoading.value = false
        }
    }
    
    fun clearDownloadSuccess() {
        _downloadSuccess.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Train2SendApp)
                OnlinePlanViewModel(app.onlinePlanRepository)
            }
        }
    }
}
