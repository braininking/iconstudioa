package com.example.worker

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.example.receiver.PackageChangeReceiver

class PackageMonitorManager(private val context: Context) {

    private var dynamicReceiver: PackageChangeReceiver? = null

    fun startMonitoring() {
        if (dynamicReceiver == null) {
            val receiver = PackageChangeReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            dynamicReceiver = receiver
        }
        IconSyncWorker.schedulePeriodicSync(context)
    }

    fun stopMonitoring() {
        dynamicReceiver?.let {
            runCatching { context.unregisterReceiver(it) }
            dynamicReceiver = null
        }
        IconSyncWorker.cancelPeriodicSync(context)
    }
}
