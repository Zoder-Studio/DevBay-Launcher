package com.devbay.launcher.notification

import android.content.Context

class NotificationAccessPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasAskedBefore(): Boolean = preferences.getBoolean(KEY_ASKED, false)

    fun markAsked() {
        preferences.edit().putBoolean(KEY_ASKED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_notification_access"
        private const val KEY_ASKED = "asked_before"
    }
}