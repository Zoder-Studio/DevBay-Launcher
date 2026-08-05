package com.devbay.launcher.app

sealed class LauncherListItem {
    data class Header(val category: AppCategory, val count: Int) : LauncherListItem()
    data class AppItem(val app: AppInfo) : LauncherListItem()
}