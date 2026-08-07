package com.devbay.launcher.icon

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class IconPackRepository(private val context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getInstalledIconPacks(): List<IconPackInfo> {
        val packageManager = context.packageManager
        val found = LinkedHashMap<String, IconPackInfo>()

        MARKER_ACTIONS.forEach { action ->
            val results = packageManager.queryIntentActivities(Intent(action), PackageManager.MATCH_ALL)
            results.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (!found.containsKey(packageName)) {
                    found[packageName] = IconPackInfo(packageName, resolveInfo.loadLabel(packageManager).toString())
                }
            }
        }
        return found.values.toList()
    }

    fun getSelectedIconPack(): String? = preferences.getString(KEY_SELECTED, null)

    fun setSelectedIconPack(packageName: String?) {
        preferences.edit().apply {
            if (packageName == null) remove(KEY_SELECTED) else putString(KEY_SELECTED, packageName)
        }.apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_icon_pack"
        private const val KEY_SELECTED = "selected_icon_pack"
        private val MARKER_ACTIONS = listOf(
            "com.novalauncher.THEME",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "org.adw.launcher.THEMES"
        )
    }
}