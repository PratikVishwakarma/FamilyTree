package com.pratik.learning.familyTree.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.utils.SyncPrefs.getIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired

class SyncOnExitWorker(val context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    lateinit var repository : FamilyTreeRepository
    override suspend fun doWork(): Result {
        return try {
            if (!getIsDataUpdateRequired(context)) {
                logger("SyncWorker", "No data update required")
                return Result.success()
            }

            repository.syncDataToFirebase()
            setIsDataUpdateRequired(context, false)
            Result.success()
        } catch (e: Exception) {
            logger("SyncWorker", "onFailure with error: ${e.message}")
            Result.failure()
        }
    }
}