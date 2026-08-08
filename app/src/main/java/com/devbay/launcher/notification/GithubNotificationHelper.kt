package com.devbay.launcher.notification

import com.devbay.launcher.R
import com.devbay.launcher.github.*
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object GitHubNotificationHelper {

    const val CHANNEL_ID = "github_monitor"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.github_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    fun notifyNewItems(context: Context, repo: GitHubWatchedRepo, delta: Int) {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${repo.fullName}"))
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            repo.fullName.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_github)
            .setContentTitle(repo.fullName)
            .setContentText(context.getString(R.string.github_notification_body, delta))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(repo.fullName.hashCode(), notification)
    }
}