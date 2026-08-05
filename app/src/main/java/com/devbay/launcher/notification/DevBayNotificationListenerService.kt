package com.devbay.launcher.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class DevBayNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshCounts()
    }

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        super.onNotificationPosted(statusBarNotification)
        refreshCounts()
    }

    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {
        super.onNotificationRemoved(statusBarNotification)
        refreshCounts()
    }

    private fun refreshCounts() {
        val currentNotifications = try {
            activeNotifications
        } catch (throwable: Throwable) {
            emptyArray()
        }

        val counts = currentNotifications
            .filterNot { it.isOngoing }
            .groupingBy { it.packageName }
            .eachCount()

        NotificationBadgeStore.updateCounts(counts)
    }
}