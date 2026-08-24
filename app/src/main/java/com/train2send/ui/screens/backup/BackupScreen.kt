package com.train2send.ui.screens.backup

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.train2send.Train2SendApp
import com.train2send.data.backup.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class BackupTab(val label: String) {
    EXPORT("Export"),
    IMPORT("Import"),
    ONLINE("Online Plans")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    onlinePlanViewModel: OnlinePlanViewModel = viewModel(factory = OnlinePlanViewModel.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as Train2SendApp
    val scope = rememberCoroutineScope()
    val backupManager = remember { BackupManager(app) }

    val onlinePlansState by onlinePlanViewModel.uiState.collectAsStateWithLifecycle()
    val downloadSuccess by onlinePlanViewModel.downloadSuccess.collectAsStateWithLifecycle()

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(BackupTab.EXPORT) }

    LaunchedEffect(downloadSuccess) {
        downloadSuccess?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onlinePlanViewModel.clearDownloadSuccess()
        }
    }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isImporting = true
                try {
                    val jsonString = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                            ?: throw Exception("Could not read file")
                    }
                    val result = backupManager.importFromJson(jsonString)
                    lastResult = "Imported ${result.exercisesImported} exercises, ${result.plansImported} plans"
                    Toast.makeText(context, lastResult, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    lastResult = "Import failed: ${e.message}"
                    Toast.makeText(context, lastResult, Toast.LENGTH_LONG).show()
                } finally {
                    isImporting = false
                }
            }
        }
    }

    // File creator for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isExporting = true
                try {
                    val jsonString = backupManager.exportToJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                            it.write(jsonString)
                        } ?: throw Exception("Could not write file")
                    }
                    lastResult = "Export successful!"
                    Toast.makeText(context, lastResult, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    lastResult = "Export failed: ${e.message}"
                    Toast.makeText(context, lastResult, Toast.LENGTH_LONG).show()
                } finally {
                    isExporting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                BackupTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    BackupTab.EXPORT -> {
                        // Export section
                        Text(
                            text = "Export",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Save all your exercises and plans as a JSON file",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Export to file
                            Button(
                                onClick = {
                                    val timestamp = LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    exportLauncher.launch("train2send-backup-$timestamp.json")
                                },
                                enabled = !isExporting,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save to File")
                            }

                            // Share
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isExporting = true
                                        try {
                                            val jsonString = backupManager.exportToJson()
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_TEXT, jsonString)
                                                putExtra(Intent.EXTRA_SUBJECT, "Train2Send Backup")
                                            }
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "Share Backup")
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Share failed: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } finally {
                                            isExporting = false
                                        }
                                    }
                                },
                                enabled = !isExporting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share")
                            }
                        }

                        // Info card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "The backup file includes all exercises, plans, day configurations, and planned exercises. You can share the file via email, messaging apps, or cloud storage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    BackupTab.IMPORT -> {
                        // Import section
                        Text(
                            text = "Import",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Restore from a previously exported JSON file. Existing data with the same IDs will be overwritten.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            enabled = !isImporting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.FileOpen, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import from File")
                        }
                    }

                    BackupTab.ONLINE -> {
                        // Online Plans section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Online Plans",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onlinePlanViewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }

                        Text(
                            text = "Download predefined plans from the official repository.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when (val state = onlinePlansState) {
                            is OnlinePlansUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tap refresh to check for online plans",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            is OnlinePlansUiState.Error -> {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            is OnlinePlansUiState.Success -> {
                                state.plans.forEach { item ->
                                    OnlinePlanCard(
                                        item = item,
                                        onDownload = { onlinePlanViewModel.downloadPlan(item) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Result display
                lastResult?.let { result ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.startsWith("Import failed") || result.startsWith("Export failed"))
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (result.contains("failed")) Icons.Default.Error
                                else Icons.Default.CheckCircle,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlinePlanCard(
    item: OnlinePlanItem,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.file.name.removeSuffix(".json").replace("_", " "),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when (item.status) {
                        PlanStatus.NEW -> "Available online"
                        PlanStatus.UPDATE -> "Update available"
                        PlanStatus.INSTALLED -> "Installed"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.status == PlanStatus.UPDATE)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = when (item.status) {
                        PlanStatus.NEW -> Icons.Default.Download
                        PlanStatus.UPDATE -> Icons.Default.Update
                        PlanStatus.INSTALLED -> Icons.Default.Refresh
                    },
                    contentDescription = "Download",
                    tint = if (item.status == PlanStatus.INSTALLED)
                        MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
