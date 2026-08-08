package com.devbay.launcher.settings

import com.devbay.launcher.R
import android.content.Context
import android.content.Intent
import android.provider.Settings

class SettingsShortcutRepository(private val context: Context) {

    fun getAvailableShortcuts(): List<SettingsShortcut> {
        return allShortcuts().filter { shortcut -> isResolvable(shortcut.intentAction) }
    }

    private fun isResolvable(action: String): Boolean {
        return Intent(action).resolveActivity(context.packageManager) != null
    }

    private fun allShortcuts(): List<SettingsShortcut> {
        return listOf(
            SettingsShortcut(
                context.getString(R.string.shortcut_developer_options),
                R.drawable.ic_developer_settings,
                Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_app_info),
                R.drawable.ic_shortcut_apps,
                Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_usage_access),
                R.drawable.ic_shortcut_usage,
                Settings.ACTION_USAGE_ACCESS_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_battery),
                R.drawable.ic_shortcut_battery,
                Intent.ACTION_POWER_USAGE_SUMMARY
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_storage),
                R.drawable.ic_shortcut_storage,
                Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_display),
                R.drawable.ic_shortcut_display,
                Settings.ACTION_DISPLAY_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_network),
                R.drawable.ic_shortcut_network,
                Settings.ACTION_WIRELESS_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_wifi),
                R.drawable.ic_shortcut_wifi,
                Settings.ACTION_WIFI_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_bluetooth),
                R.drawable.ic_shortcut_bluetooth,
                Settings.ACTION_BLUETOOTH_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_notifications),
                R.drawable.ic_shortcut_notifications,
                Settings.ACTION_APP_NOTIFICATION_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_security),
                R.drawable.ic_shortcut_security,
                Settings.ACTION_SECURITY_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_accessibility),
                R.drawable.ic_shortcut_accessibility,
                Settings.ACTION_ACCESSIBILITY_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_sound),
                R.drawable.ic_shortcut_sound,
                Settings.ACTION_SOUND_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_date_time),
                R.drawable.ic_shortcut_date_time,
                Settings.ACTION_DATE_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_about_phone),
                R.drawable.ic_shortcut_info,
                Settings.ACTION_DEVICE_INFO_SETTINGS
            ),
            SettingsShortcut(
                context.getString(R.string.shortcut_home_app),
                R.drawable.ic_shortcut_home,
                Settings.ACTION_HOME_SETTINGS
            )
        )
    }
}