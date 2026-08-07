package com.devbay.launcher

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DevBayLauncherApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        val themePreferences = ThemePreferences(this)
        AppCompatDelegate.setDefaultNightMode(themePreferences.getNightMode())

        val packageChangeReceiver = PackageChangeReceiver(applicationScope)
        ContextCompat.registerReceiver(
            this,
            packageChangeReceiver,
            PackageChangeReceiver.intentFilter(),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val screenStateReceiver = ScreenStateReceiver()
        ContextCompat.registerReceiver(
            this,
            screenStateReceiver,
            ScreenStateReceiver.intentFilter(),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        applicationScope.launch {
            AppCacheRefresher.refresh(applicationContext)
        }
    }
}