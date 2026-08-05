package com.devbay.launcher.recent

import android.content.Context

class RecentAppsRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getRecentKeys(): List<String> {
        val raw = preferences.getString(KEY_RECENT, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(DELIMITER)
    }

    fun recordLaunch(appKey: String) {
        val current = getRecentKeys().toMutableList()
        current.remove(appKey)
        current.add(0, appKey)
        while (current.size > MAX_RECENT) {
            current.removeAt(current.size - 1)
        }
        saveKeys(current)
    }

    fun removeKey(appKey: String) {
        val current = getRecentKeys().toMutableList()
        if (current.remove(appKey)) saveKeys(current)
    }

    fun clearAll() {
        saveKeys(emptyList())
    }

    private fun saveKeys(keys: List<String>) {
        preferences.edit().putString(KEY_RECENT, keys.joinToString(DELIMITER)).apply()
    }

    companion object {
        const val MAX_RECENT = 10
        private const val PREFS_NAME = "devbay_recent_apps"
        private const val KEY_RECENT = "recent_app_keys"
        private const val DELIMITER = "|"
    }
}