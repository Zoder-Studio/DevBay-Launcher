package com.devbay.launcher.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemePreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeOption(): ThemeOption {
        val stored = preferences.getString(KEY_THEME, ThemeOption.SYSTEM.storageKey)
        return ThemeOption.entries.firstOrNull { it.storageKey == stored } ?: ThemeOption.SYSTEM
    }

    fun setThemeOption(option: ThemeOption) {
        preferences.edit().putString(KEY_THEME, option.storageKey).apply()
    }

    fun getNightMode(): Int {
        return when (getThemeOption()) {
            ThemeOption.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeOption.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeOption.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    companion object {
        private const val PREFS_NAME = "devbay_theme"
        private const val KEY_THEME = "theme_option"
    }
}