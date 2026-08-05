package com.devbay.launcher.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationBadgeStore {

    private val counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val countsFlow: StateFlow<Map<String, Int>> = counts.asStateFlow()

    fun updateCounts(newCounts: Map<String, Int>) {
        counts.value = newCounts
    }

    fun getCounts(): Map<String, Int> = counts.value
}