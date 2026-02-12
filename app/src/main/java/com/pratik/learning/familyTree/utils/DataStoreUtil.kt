package com.pratik.learning.familyTree.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.pratik.learning.familyTree.FamilyTreeApp
import com.pratik.learning.familyTree.FamilyTreeApp.Companion.isAdmin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "sync_prefs"
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

object SyncPrefs {
    private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    private val IS_DATA_MODIFIED = booleanPreferencesKey("is_data_modified")
    private val CURRENT_USER_ID = intPreferencesKey("current_user_id")

    private val FAV_MEMBER_IDS = stringSetPreferencesKey("fav_member_ids")

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

    suspend fun getMyUerID(context: Context): Int {
        return context.dataStore.data
            .map { prefs -> prefs[CURRENT_USER_ID] ?: -1 }
            .first()
    }

    suspend fun setMyUserID(context: Context, value: Int) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_USER_ID] = value
        }
    }

    suspend fun getMyFavMemberIDs(context: Context): List<Int> {
        return context.dataStore.data
            .map { prefs ->
                prefs[FAV_MEMBER_IDS]
                    ?.mapNotNull { it.toIntOrNull() }
                    ?: emptyList()
            }
            .first()
    }

    suspend fun setMyFavMemberIDs(
        context: Context,
        ids: List<Int>
    ) {
        context.dataStore.edit { prefs ->
            prefs[FAV_MEMBER_IDS] = ids.map { it.toString() }.toSet()
        }
    }

    suspend fun addIdToMyFavList(
        context: Context,
        id: Int
    ) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAV_MEMBER_IDS] ?: emptySet()
            prefs[FAV_MEMBER_IDS] = current + id.toString()
        }
    }

    suspend fun removeIdFromMyFavList(
        context: Context,
        id: Int
    ) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAV_MEMBER_IDS] ?: emptySet()
            prefs[FAV_MEMBER_IDS] = current - id.toString()
        }
    }

    fun isMemberInMyFavListFlow(
        context: Context,
        id: Int
    ): Flow<Boolean> {
        return context.dataStore.data
            .map { prefs ->
                prefs[FAV_MEMBER_IDS]
                    ?.contains(id.toString())
                    ?: false
            }
    }

}
