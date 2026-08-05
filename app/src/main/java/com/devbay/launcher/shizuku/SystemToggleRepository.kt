package com.devbay.launcher.shizuku

import android.content.Context
import android.provider.Settings

class SystemToggleRepository(private val context: Context) {

    fun isDeveloperSettingsEnabled(): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
    }

    fun isSlowAnimationsEnabled(): Boolean {
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        return scale >= SLOW_ANIMATION_SCALE
    }

    fun isBigFontEnabled(): Boolean {
        val scale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
        return scale >= BIG_FONT_SCALE
    }

    fun isWirelessAdbEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, "adb_wifi_enabled", 0) == 1
        } catch (throwable: Throwable) {
            false
        }
    }

    fun setDeveloperSettingsCommand(enable: Boolean): String =
        "settings put global development_settings_enabled ${if (enable) 1 else 0}"

    fun setSlowAnimationsCommand(enable: Boolean): String {
        val scale = if (enable) SLOW_ANIMATION_SCALE else 1f
        return "settings put global window_animation_scale $scale && " +
            "settings put global transition_animation_scale $scale && " +
            "settings put global animator_duration_scale $scale"
    }

    fun setBigFontCommand(enable: Boolean): String {
        val scale = if (enable) BIG_FONT_SCALE else 1f
        return "settings put system font_scale $scale"
    }

    fun setWirelessAdbCommand(enable: Boolean): String =
        "settings put global adb_wifi_enabled ${if (enable) 1 else 0}"

    fun killAllBackgroundAppsCommand(): String = "am kill-all"

    companion object {
        private const val SLOW_ANIMATION_SCALE = 5f
        private const val BIG_FONT_SCALE = 1.3f
    }
}