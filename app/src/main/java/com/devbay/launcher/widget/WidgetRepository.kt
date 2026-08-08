package com.devbay.launcher.widget

import android.content.Context

class WidgetRepository(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getWidgetIds(): List<Int> {
        val raw = preferences.getString(KEY_WIDGET_IDS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(DELIMITER).mapNotNull { it.toIntOrNull() }
    }

    fun addWidgetId(widgetId: Int) {
        val current = getWidgetIds().toMutableList()
        if (!current.contains(widgetId)) {
            current.add(widgetId)
            saveWidgetIds(current)
        }
    }

    fun removeWidgetId(widgetId: Int) {
        val current = getWidgetIds().toMutableList()
        if (current.remove(widgetId)) {
            saveWidgetIds(current)
        }
    }

    private fun saveWidgetIds(ids: List<Int>) {
        preferences.edit().putString(KEY_WIDGET_IDS, ids.joinToString(DELIMITER)).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_widgets"
        private const val KEY_WIDGET_IDS = "widget_ids"
        private const val DELIMITER = ","
    }
}