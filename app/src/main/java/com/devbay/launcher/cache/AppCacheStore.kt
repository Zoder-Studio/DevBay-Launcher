package com.devbay.launcher.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppCacheStore {

    private val apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val appsFlow: StateFlow<List<AppInfo>> = apps.asStateFlow()

    fun updateApps(newApps: List<AppInfo>) {
        apps.value = newApps
    }

    fun getApps(): List<AppInfo> = apps.value
}