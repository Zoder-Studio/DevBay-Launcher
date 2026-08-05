package com.devbay.launcher.quicktoggle

enum class QuickToggleType {
    DEVELOPER_SETTINGS,
    KILL_ACTIVITIES,
    SLOW_ANIMATIONS,
    BIG_FONTS,
    WIRELESS_ADB
}

data class QuickToggleChip(
    val type: QuickToggleType,
    val label: String,
    val iconRes: Int,
    val isToggle: Boolean,
    val isActive: Boolean
)