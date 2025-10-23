package com.pratik.learning.familyTree

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pratik.learning.familyTree.utils.SyncOnExitWorker
import com.pratik.learning.familyTree.utils.isAdmin
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FamilyTreeApp : Application(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("FamilyTreeApp", "App in background calling enqueueSyncWork if Admin: $isAdmin")
        if (isAdmin)
            enqueueSyncWork()
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