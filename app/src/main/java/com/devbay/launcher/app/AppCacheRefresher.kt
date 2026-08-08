package com.devbay.launcher.app

import com.devbay.launcher.icon.*
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppCacheRefresher {

    suspend fun refresh(context: Context) {
        val appContext = context.applicationContext
        val themedApps = withContext(Dispatchers.Default) {
            val rawApps = AppRepository(appContext).loadInstalledApps()
            IconApplier.applyIconPack(appContext, rawApps)
        }
        AppCacheStore.updateApps(themedApps)
    }
}