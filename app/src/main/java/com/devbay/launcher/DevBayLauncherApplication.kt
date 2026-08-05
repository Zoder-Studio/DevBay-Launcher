package com.devbay.launcher

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class DevBayLauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val themePreferences = ThemePreferences(this)
        AppCompatDelegate.setDefaultNightMode(themePreferences.getNightMode())
    }
}