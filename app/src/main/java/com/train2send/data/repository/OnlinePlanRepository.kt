package com.train2send.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.train2send.data.backup.BackupManager
import com.train2send.data.model.GitHubFile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import android.util.Log
import java.io.Closeable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.onlinePlansDataStore: DataStore<Preferences> by preferencesDataStore(name = "online_plans_cache")

class OnlinePlanRepository(
    private val context: Context,
    private val backupManager: BackupManager
) : Closeable {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private object PreferencesKeys {
        val PLAN_SHAS = stringPreferencesKey("plan_shas")
    }

    private val GITHUB_API_URL = "https://api.github.com/repos/luciangutu/train2send/contents/app/src/main/assets"

    /**
     * Fetch list of JSON files from the GitHub repository.
     */
    suspend fun fetchOnlinePlans(): List<GitHubFile> {
        return try {
            Log.d("OnlinePlanRepo", "Fetching from $GITHUB_API_URL")
            val files: List<GitHubFile> = client.get(GITHUB_API_URL).body()
            Log.d("OnlinePlanRepo", "Found ${files.size} files")
            files.forEach { Log.d("OnlinePlanRepo", "File: ${it.name}") }
            files.filter { it.name.endsWith(".json") && it.type == "file" }
        } catch (e: Exception) {
            Log.e("OnlinePlanRepo", "Error fetching plans", e)
            emptyList()
        }
    }

    /**
     * Download a specific plan and import it via BackupManager.
     */
    suspend fun downloadAndImportPlan(file: GitHubFile): Result<Unit> {
        val downloadUrl = file.downloadUrl ?: return Result.failure(Exception("No download URL"))
        
        return try {
            val jsonString: String = client.get(downloadUrl).body()
            backupManager.importFromJson(jsonString)
            savePlanSha(file.name, file.sha)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the map of locally installed plan filenames to their GitHub SHAs.
     */
    val installedPlanShas: Flow<Map<String, String>> = context.onlinePlansDataStore.data.map { preferences ->
        val jsonString = preferences[PreferencesKeys.PLAN_SHAS] ?: "{}"
        try {
            json.decodeFromString<Map<String, String>>(jsonString)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun savePlanSha(fileName: String, sha: String) {
        context.onlinePlansDataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.PLAN_SHAS] ?: "{}"
            val currentMap = try {
                json.decodeFromString<MutableMap<String, String>>(currentJson)
            } catch (e: Exception) {
                mutableMapOf()
            }
            currentMap[fileName] = sha
            preferences[PreferencesKeys.PLAN_SHAS] = json.encodeToString(currentMap as Map<String, String>)
        }
    }

    override fun close() {
        client.close()
    }
}
