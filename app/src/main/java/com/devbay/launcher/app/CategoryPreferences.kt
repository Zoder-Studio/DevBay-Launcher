package com.devbay.launcher.app

import android.content.Context

class CategoryPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPinnedPackages(): List<String> = readOrderedList(KEY_PINNED)

    fun getToolsPackages(): List<String> = readOrderedList(KEY_TOOLS)

    fun pinApp(appKey: String) {
        val current = readOrderedList(KEY_PINNED).toMutableList()
        if (!current.contains(appKey)) {
            current.add(appKey)
            writeOrderedList(KEY_PINNED, current)
        }
    }

    fun unpinApp(appKey: String) {
        val current = readOrderedList(KEY_PINNED).toMutableList()
        if (current.remove(appKey)) {
            writeOrderedList(KEY_PINNED, current)
        }
    }

    fun addToTools(appKey: String) {
        val current = readOrderedList(KEY_TOOLS).toMutableList()
        if (!current.contains(appKey)) {
            current.add(appKey)
            writeOrderedList(KEY_TOOLS, current)
        }
    }

    fun removeFromTools(appKey: String) {
        val current = readOrderedList(KEY_TOOLS).toMutableList()
        if (current.remove(appKey)) {
            writeOrderedList(KEY_TOOLS, current)
        }
    }

    fun isPinned(appKey: String): Boolean = readOrderedList(KEY_PINNED).contains(appKey)

    fun isTools(appKey: String): Boolean = readOrderedList(KEY_TOOLS).contains(appKey)

    private fun readOrderedList(key: String): List<String> {
        val raw = preferences.getString(key, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(DELIMITER)
    }

    private fun writeOrderedList(key: String, values: List<String>) {
        preferences.edit().putString(key, values.joinToString(DELIMITER)).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_categories"
        private const val KEY_PINNED = "pinned_apps"
        private const val KEY_TOOLS = "tools_apps"
        private const val DELIMITER = "|"
    }
}