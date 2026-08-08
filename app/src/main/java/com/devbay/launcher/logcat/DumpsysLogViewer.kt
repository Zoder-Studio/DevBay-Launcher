package com.devbay.launcher.logcat

import com.devbay.launcher.shizuku.*

class DumpsysLogViewer {

    suspend fun fetchRecentCrashLog(packageName: String): String {
        val command = "logcat -d -b crash --pid=\$(pidof $packageName || echo 0) -t 200"
        val result = ShizukuCommandExecutor.execute(command)
        return if (result.success) result.output else result.output.ifBlank {
            "No crash log available or Shizuku permission missing."
        }
    }

    suspend fun fetchRecentLogcat(packageName: String): String {
        val command = "logcat -d -t 300 | grep -i \"$packageName\""
        val result = ShizukuCommandExecutor.execute(command)
        return if (result.success) result.output else result.output.ifBlank {
            "No logs available or Shizuku permission missing."
        }
    }
}