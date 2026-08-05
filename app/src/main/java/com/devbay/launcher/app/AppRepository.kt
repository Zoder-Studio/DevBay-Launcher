package com.devbay.launcher.app

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import java.util.Locale

class AppRepository(private val context: Context) {

    fun loadInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedActivities = packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_ALL
        )

        return resolvedActivities
            .mapNotNull { resolveInfo -> buildAppInfo(packageManager, resolveInfo) }
            .distinctBy { it.key }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun buildAppInfo(packageManager: PackageManager, resolveInfo: ResolveInfo): AppInfo? {
        val packageName = resolveInfo.activityInfo.packageName
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val applicationInfo = packageInfo.applicationInfo ?: return null
            val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            AppInfo(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = packageName,
                activityName = resolveInfo.activityInfo.name,
                icon = resolveInfo.loadIcon(packageManager),
                isDebuggable = isDebuggable,
                versionName = packageInfo.versionName ?: "-",
                versionCode = versionCode
            )
        } catch (exception: PackageManager.NameNotFoundException) {
            null
        }
    }
}