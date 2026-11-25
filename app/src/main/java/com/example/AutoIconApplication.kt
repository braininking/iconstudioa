package com.example

import android.app.Application
import com.example.data.repository.AutoIconRepository
import com.example.worker.PackageMonitorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AutoIconApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var repository: AutoIconRepository
        private set
    lateinit var monitorManager: PackageMonitorManager
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AutoIconRepository(this)
        monitorManager = PackageMonitorManager(this)

        appScope.launch {
            repository.ensureDefaultThemesSeeded()
            val settings = repository.settingsFlow.first()
            if (settings.autoDetectionEnabled) {
                monitorManager.startMonitoring()
            }
            repository.syncInstalledApps(forceReprocessAll = false)
        }
    }
}
