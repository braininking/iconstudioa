package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.model.DetectionEventType
import com.example.data.repository.AutoIconRepository
import java.util.concurrent.TimeUnit

class IconSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = AutoIconRepository(applicationContext)
            val targetPackage = inputData.getString(KEY_PACKAGE_NAME)
            val eventTypeName = inputData.getString(KEY_EVENT_TYPE)
            val eventType = eventTypeName?.let { name ->
                DetectionEventType.entries.firstOrNull { it.name == name }
            }

            if (!targetPackage.isNullOrBlank() && eventType != null) {
                repository.handlePackageChanged(targetPackage, eventType)
            } else {
                repository.syncInstalledApps(forceReprocessAll = false)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val KEY_PACKAGE_NAME = "key_package_name"
        private const val KEY_EVENT_TYPE = "key_event_type"
        private const val PERIODIC_WORK_NAME = "autoicon_periodic_package_sync"

        fun enqueueSinglePackage(
            context: Context,
            packageName: String,
            eventType: DetectionEventType
        ) {
            val data = Data.Builder()
                .putString(KEY_PACKAGE_NAME, packageName)
                .putString(KEY_EVENT_TYPE, eventType.name)
                .build()
            val request = OneTimeWorkRequestBuilder<IconSyncWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "pkg_sync_${packageName}_${eventType.name}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun enqueueImmediateFullSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<IconSyncWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "autoicon_manual_sync",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun schedulePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<IconSyncWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        }
    }
}
