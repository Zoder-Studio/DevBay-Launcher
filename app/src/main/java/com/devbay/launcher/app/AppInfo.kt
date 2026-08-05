package com.devbay.launcher.app

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable,
    val isDebuggable: Boolean,
    val versionName: String,
    val versionCode: Long
) {
    val key: String get() = "$packageName/$activityName"
}