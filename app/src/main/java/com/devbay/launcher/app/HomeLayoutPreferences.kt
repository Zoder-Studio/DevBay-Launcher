package com.devbay.launcher.app

import android.content.Context

enum class HomeLayoutMode { FULL, MINIMAL }

class HomeLayoutPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMode(): HomeLayoutMode {
        val stored = preferences.getString(KEY_MODE, HomeLayoutMode.FULL.name)
        return HomeLayoutMode.entries.firstOrNull { it.name == stored } ?: HomeLayoutMode.FULL
    }

    fun setMode(mode: HomeLayoutMode) {
        preferences.edit().putString(KEY_MODE, mode.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_home_layout"
        private const val KEY_MODE = "mode"
    }
}