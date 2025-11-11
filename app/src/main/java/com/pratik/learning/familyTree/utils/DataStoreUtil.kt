package com.pratik.learning.familyTree.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "sync_prefs"
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

object SyncPrefs {
    private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    private val IS_DATA_MODIFIED = booleanPreferencesKey("is_data_modified")

    suspend fun getLastSyncTime(context: Context): Long {
        return context.dataStore.data
            .map { prefs -> prefs[LAST_SYNC_TIME] ?: 0L }
            .first()
    }

    suspend fun setLastSyncTime(context: Context, timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = timestamp
        }
    }

    suspend fun shouldSync(context: Context): Boolean {
        val lastSync = getLastSyncTime(context)
        val currentTime = System.currentTimeMillis()
//        val threeHoursMillis = 3 * 60 * 60 * 1000
        val threeHoursMillis = if (isAdmin) 1 * 5 * 60 * 1000 else 3 * 60 * 60 * 1000
        val isSyncRequired = currentTime - lastSync >= threeHoursMillis
//        if (isSyncRequired)
//            setLastSyncTime(context, currentTime)
        return isSyncRequired
    }

    suspend fun getIsDataUpdateRequired(context: Context): Boolean {
        return context.dataStore.data
            .map { prefs -> prefs[IS_DATA_MODIFIED] ?: false }
            .first()
    }

    suspend fun setIsDataUpdateRequired(context: Context, value: Boolean) {
        if (!isAdmin) return
        context.dataStore.edit { prefs ->
            prefs[IS_DATA_MODIFIED] = value
        }
    }
}
