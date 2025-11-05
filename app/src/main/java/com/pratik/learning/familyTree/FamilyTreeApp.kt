package com.pratik.learning.familyTree

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pratik.learning.familyTree.utils.SyncOnExitWorker
import com.pratik.learning.familyTree.utils.SyncPrefs.getIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.isAdmin
import com.pratik.learning.familyTree.utils.logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

@HiltAndroidApp
class FamilyTreeApp : Application(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        logger("App in background calling enqueueSyncWork if Admin: $isAdmin")
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            if (isAdmin && getIsDataUpdateRequired(this@FamilyTreeApp)) {
                logger("App in background calling upload is required")
                enqueueSyncWork()
            } else {
                logger("App in background not called but upload not required")
            }
        }
    }

    private fun enqueueSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<SyncOnExitWorker>()
            .setConstraints(constraints = constraints)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

}