package com.devbay.launcher.gesture

import android.content.Context

object GestureActionCodec {

    fun encode(action: GestureAction): String {
        return when (action) {
            GestureAction.None -> "NONE"
            GestureAction.OpenSearch -> "OPEN_SEARCH"
            GestureAction.OpenSettings -> "OPEN_SETTINGS"
            GestureAction.OpenClipboard -> "OPEN_CLIPBOARD"
            GestureAction.OpenNotificationPanel -> "OPEN_NOTIFICATION_PANEL"
            GestureAction.OpenQuickSettingsPanel -> "OPEN_QUICK_SETTINGS_PANEL"
            is GestureAction.LaunchApp -> "LAUNCH_APP|${action.packageName}|${action.activityName}"
        }
    }

    fun decode(raw: String?): GestureAction {
        if (raw.isNullOrBlank()) return GestureAction.None
        val parts = raw.split("|")
        return when (parts[0]) {
            "OPEN_SEARCH" -> GestureAction.OpenSearch
            "OPEN_SETTINGS" -> GestureAction.OpenSettings
            "OPEN_CLIPBOARD" -> GestureAction.OpenClipboard
            "OPEN_NOTIFICATION_PANEL" -> GestureAction.OpenNotificationPanel
            "OPEN_QUICK_SETTINGS_PANEL" -> GestureAction.OpenQuickSettingsPanel
            "LAUNCH_APP" -> if (parts.size == 3) GestureAction.LaunchApp(parts[1], parts[2]) else GestureAction.None
            else -> GestureAction.None
        }
    }

    fun displayLabel(context: Context, action: GestureAction, apps: List<AppInfo>): String {
        return when (action) {
            GestureAction.None -> context.getString(R.string.gesture_action_none)
            GestureAction.OpenSearch -> context.getString(R.string.gesture_action_open_search)
            GestureAction.OpenSettings -> context.getString(R.string.gesture_action_open_settings)
            GestureAction.OpenClipboard -> context.getString(R.string.gesture_action_open_clipboard)
            GestureAction.OpenNotificationPanel -> context.getString(R.string.gesture_action_open_notifications)
            GestureAction.OpenQuickSettingsPanel -> context.getString(R.string.gesture_action_open_quick_settings)
            is GestureAction.LaunchApp -> {
                val app = apps.firstOrNull { it.packageName == action.packageName && it.activityName == action.activityName }
                app?.label ?: context.getString(R.string.gesture_action_app_removed)
            }
        }
    }
}