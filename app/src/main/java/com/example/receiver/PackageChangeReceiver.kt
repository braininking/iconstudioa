package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.DetectionEventType
import com.example.worker.IconSyncWorker

class PackageChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val packageName = intent.data?.schemeSpecificPart ?: return
        if (packageName == context.packageName) return

        val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        val eventType = when (action) {
            Intent.ACTION_PACKAGE_ADDED -> if (isReplacing) return else DetectionEventType.ADDED
            Intent.ACTION_PACKAGE_REPLACED -> DetectionEventType.REPLACED
            Intent.ACTION_PACKAGE_REMOVED -> if (isReplacing) return else DetectionEventType.REMOVED
            else -> return
        }

        IconSyncWorker.enqueueSinglePackage(
            context = context.applicationContext,
            packageName = packageName,
            eventType = eventType
        )
    }
}
