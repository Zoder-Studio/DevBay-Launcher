package com.devbay.launcher.launcher

sealed class LauncherListItem {
    data class Header(val category: AppCategory, val count: Int) : LauncherListItem()
    data class AppItem(val app: AppInfo) : LauncherListItem()
    data class FolderItem(val folder: AppFolder, val previewApps: List<AppInfo>) : LauncherListItem()
}