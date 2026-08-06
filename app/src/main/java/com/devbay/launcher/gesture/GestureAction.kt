package com.devbay.launcher.gesture

sealed class GestureAction {
    object None : GestureAction()
    object OpenSearch : GestureAction()
    object OpenSettings : GestureAction()
    object OpenClipboard : GestureAction()
    object OpenNotificationPanel : GestureAction()
    object OpenQuickSettingsPanel : GestureAction()
    data class LaunchApp(val packageName: String, val activityName: String) : GestureAction()
}