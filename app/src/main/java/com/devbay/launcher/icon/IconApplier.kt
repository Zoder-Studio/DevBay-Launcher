package com.devbay.launcher.icon

import com.devbay.launcher.app.*
import android.content.Context

object IconApplier {

    fun applyIconPack(context: Context, apps: List<AppInfo>): List<AppInfo> {
        val iconPackRepository = IconPackRepository(context)
        val selectedPackage = iconPackRepository.getSelectedIconPack() ?: return apps

        val parser = IconPackParser(context)
        val iconMap = parser.loadIconMap(selectedPackage)
        if (iconMap.isEmpty()) return apps

        return apps.map { app ->
            val drawableName = iconMap[app.key] ?: return@map app
            val themedDrawable = parser.loadDrawable(selectedPackage, drawableName) ?: return@map app
            app.copy(icon = themedDrawable)
        }
    }
}