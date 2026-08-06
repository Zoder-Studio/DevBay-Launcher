package com.devbay.launcher.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.devbay.launcher.databinding.ActivityGestureSettingsBinding

class GestureSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestureSettingsBinding
    private lateinit var gesturePreferences: GesturePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestureSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gesturePreferences = GesturePreferences(applicationContext)

        binding.closeButton.setOnClickListener { finish() }
        binding.swipeLeftRow.setOnClickListener { showActionPicker(GestureDirection.SWIPE_LEFT) }
        binding.swipeRightRow.setOnClickListener { showActionPicker(GestureDirection.SWIPE_RIGHT) }
        binding.doubleTapRow.setOnClickListener { showActionPicker(GestureDirection.DOUBLE_TAP) }

        refreshLabels()
    }

    private fun refreshLabels() {
        val apps = AppCacheStore.getApps()
        binding.swipeLeftValue.text =
            GestureActionCodec.displayLabel(this, gesturePreferences.getAction(GestureDirection.SWIPE_LEFT), apps)
        binding.swipeRightValue.text =
            GestureActionCodec.displayLabel(this, gesturePreferences.getAction(GestureDirection.SWIPE_RIGHT), apps)
        binding.doubleTapValue.text =
            GestureActionCodec.displayLabel(this, gesturePreferences.getAction(GestureDirection.DOUBLE_TAP), apps)
    }

    private fun showActionPicker(direction: GestureDirection) {
        val labels = arrayOf(
            getString(R.string.gesture_action_none),
            getString(R.string.gesture_action_open_search),
            getString(R.string.gesture_action_open_settings),
            getString(R.string.gesture_action_open_clipboard),
            getString(R.string.gesture_action_open_notifications),
            getString(R.string.gesture_action_open_quick_settings),
            getString(R.string.gesture_action_pick_app)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.gesture_pick_action_title)
            .setItems(labels) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> saveAction(direction, GestureAction.None)
                    1 -> saveAction(direction, GestureAction.OpenSearch)
                    2 -> saveAction(direction, GestureAction.OpenSettings)
                    3 -> saveAction(direction, GestureAction.OpenClipboard)
                    4 -> saveAction(direction, GestureAction.OpenNotificationPanel)
                    5 -> saveAction(direction, GestureAction.OpenQuickSettingsPanel)
                    6 -> showAppPicker(direction)
                }
            }
            .show()
    }

    private fun showAppPicker(direction: GestureDirection) {
        val apps = AppCacheStore.getApps()
        if (apps.isEmpty()) {
            Toast.makeText(this, R.string.gesture_no_apps_available, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = apps.map { it.label }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(R.string.gesture_pick_app_title)
            .setItems(labels) { dialog, which ->
                val app = apps[which]
                saveAction(direction, GestureAction.LaunchApp(app.packageName, app.activityName))
                dialog.dismiss()
            }
            .show()
    }

    private fun saveAction(direction: GestureDirection, action: GestureAction) {
        gesturePreferences.setAction(direction, action)
        refreshLabels()
    }
}