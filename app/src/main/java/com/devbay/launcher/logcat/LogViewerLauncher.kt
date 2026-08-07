package com.devbay.launcher.logcat

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object LogViewerLauncher {

    private const val LOGFOX_PACKAGE = "com.f0x1d.logfox"

    fun isLogFoxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(LOGFOX_PACKAGE, 0)
            true
        } catch (exception: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openLogFox(context: Context): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(LOGFOX_PACKAGE) ?: return false
        return try {
            context.startActivity(launchIntent)
            true
        } catch (exception: ActivityNotFoundException) {
            false
        }
    }

    fun openLogFoxOnPlayStore(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$LOGFOX_PACKAGE"))
            )
        } catch (exception: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/$LOGFOX_PACKAGE"))
            )
        }
    }
}