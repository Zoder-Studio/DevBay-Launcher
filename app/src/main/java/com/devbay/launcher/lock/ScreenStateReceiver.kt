package com.devbay.launcher.lock

import com.devbay.launcher.activity.*
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_SCREEN_ON) return

        val lockScreenPreferences = LockScreenPreferences(context)
        if (!lockScreenPreferences.isEnabled()) return

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isLocked = keyguardManager?.isKeyguardLocked ?: false
        if (!isLocked) return

        val lockScreenIntent = Intent(context, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(lockScreenIntent)
    }

    companion object {
        fun intentFilter(): IntentFilter {
            return IntentFilter(Intent.ACTION_SCREEN_ON)
        }
    }
}