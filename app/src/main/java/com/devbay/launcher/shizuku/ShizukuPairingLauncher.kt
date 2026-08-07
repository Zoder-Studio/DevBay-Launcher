package com.devbay.launcher.shizuku

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object ShizukuPairingLauncher {

    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"

    fun isShizukuAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (exception: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openShizukuApp(context: Context): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE) ?: return false
        return try {
            context.startActivity(launchIntent)
            true
        } catch (exception: ActivityNotFoundException) {
            false
        }
    }

    fun openShizukuOnStore(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$SHIZUKU_PACKAGE"))
            )
        } catch (exception: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RikkaApps/Shizuku/releases"))
            )
        }
    }
}