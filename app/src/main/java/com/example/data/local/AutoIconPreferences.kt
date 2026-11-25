package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.PreviewGridSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.autoIconDataStore: DataStore<Preferences> by preferencesDataStore(name = "autoicon_prefs")

data class AutomationSettings(
    val autoDetectionEnabled: Boolean = true,
    val autoProcessOnInstall: Boolean = true,
    val novaAutoSync: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val previewGridSize: PreviewGridSize = PreviewGridSize.MEDIUM
)

class AutoIconPreferences(private val context: Context) {

    private object Keys {
        val AUTO_DETECTION = booleanPreferencesKey("auto_detection_enabled")
        val AUTO_PROCESS_INSTALL = booleanPreferencesKey("auto_process_on_install")
        val NOVA_AUTO_SYNC = booleanPreferencesKey("nova_auto_sync")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val PREVIEW_GRID_SIZE = stringPreferencesKey("preview_grid_size")
    }

    val settingsFlow: Flow<AutomationSettings> = context.autoIconDataStore.data.map { prefs ->
        val gridName = prefs[Keys.PREVIEW_GRID_SIZE] ?: PreviewGridSize.MEDIUM.name
        AutomationSettings(
            autoDetectionEnabled = prefs[Keys.AUTO_DETECTION] ?: true,
            autoProcessOnInstall = prefs[Keys.AUTO_PROCESS_INSTALL] ?: true,
            novaAutoSync = prefs[Keys.NOVA_AUTO_SYNC] ?: true,
            lastSyncTimestamp = prefs[Keys.LAST_SYNC_TIMESTAMP] ?: 0L,
            previewGridSize = PreviewGridSize.entries.firstOrNull { it.name == gridName }
                ?: PreviewGridSize.MEDIUM
        )
    }

    suspend fun setAutoDetectionEnabled(enabled: Boolean) {
        context.autoIconDataStore.edit { it[Keys.AUTO_DETECTION] = enabled }
    }

    suspend fun setAutoProcessOnInstall(enabled: Boolean) {
        context.autoIconDataStore.edit { it[Keys.AUTO_PROCESS_INSTALL] = enabled }
    }

    suspend fun setNovaAutoSync(enabled: Boolean) {
        context.autoIconDataStore.edit { it[Keys.NOVA_AUTO_SYNC] = enabled }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.autoIconDataStore.edit { it[Keys.LAST_SYNC_TIMESTAMP] = timestamp }
    }

    suspend fun setPreviewGridSize(size: PreviewGridSize) {
        context.autoIconDataStore.edit { it[Keys.PREVIEW_GRID_SIZE] = size.name }
    }
}
