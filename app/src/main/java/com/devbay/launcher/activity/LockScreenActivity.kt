package com.devbay.launcher.activity

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.devbay.launcher.R
import com.devbay.launcher.databinding.*
import com.devbay.launcher.databinding.ActivityLockScreenBinding
import java.util.Date
import kotlin.math.abs

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private lateinit var keyguardManager: KeyguardManager

    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockUpdater = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, CLOCK_UPDATE_INTERVAL_MS)
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateBatteryLevel(intent)
        }
    }

    private var touchStartY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        onBackPressedDispatcher.addCallback(this) {
            // Intentionally ignored; back press should not bypass the lock screen.
        }

        binding.root.setOnTouchListener { _, event -> handleSwipeTouch(event) }
        binding.unlockHint.setOnClickListener { requestUnlock() }

        updateRamUsage()
    }

    override fun onResume() {
        super.onResume()
        if (!keyguardManager.isKeyguardLocked) {
            finish()
            return
        }
        clockHandler.post(clockUpdater)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateRamUsage()
    }

    override fun onPause() {
        super.onPause()
        clockHandler.removeCallbacks(clockUpdater)
        try {
            unregisterReceiver(batteryReceiver)
        } catch (throwable: IllegalArgumentException) {
            // Receiver was not registered; safe to ignore.
        }
    }

    private fun handleSwipeTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStartY = event.y
            MotionEvent.ACTION_UP -> {
                val deltaY = touchStartY - event.y
                if (deltaY > SWIPE_UP_THRESHOLD_PX) {
                    requestUnlock()
                }
            }
        }
        return true
    }

    private fun requestUnlock() {
        keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
            override fun onDismissSucceeded() {
                finish()
            }

            override fun onDismissCancelled() {
                // User cancelled system authentication; stay on the lock screen.
            }
        })
    }

    private fun updateClock() {
        val now = Date()
        binding.clockText.text = DateFormat.format(CLOCK_FORMAT, now)
        binding.dateText.text = DateFormat.format(DATE_FORMAT, now)
    }

    private fun updateBatteryLevel(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            val percentage = (level * 100) / scale
            binding.batteryText.text = getString(R.string.lock_screen_battery_format, percentage)
        }
    }

    private fun updateRamUsage() {
        val activityManager = getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val usedBytes = memoryInfo.totalMem - memoryInfo.availMem
        val usedGb = usedBytes / BYTES_IN_GIGABYTE
        val totalGb = memoryInfo.totalMem / BYTES_IN_GIGABYTE

        binding.ramText.text = getString(R.string.lock_screen_ram_format, usedGb, totalGb)
    }

    companion object {
        private const val CLOCK_FORMAT = "HH:mm"
        private const val DATE_FORMAT = "EEEE, d MMMM yyyy"
        private const val CLOCK_UPDATE_INTERVAL_MS = 1000L
        private const val SWIPE_UP_THRESHOLD_PX = 150
        private const val BYTES_IN_GIGABYTE = 1024.0 * 1024.0 * 1024.0
    }
}