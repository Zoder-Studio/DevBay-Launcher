package com.devbay.launcher.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object ShizukuCommandExecutor {

    const val REQUEST_CODE = 9001

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (throwable: Throwable) {
            false
        }
    }

    fun isPermissionGranted(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (throwable: Throwable) {
            false
        }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(REQUEST_CODE)
        } catch (throwable: Throwable) {
            // Shizuku not running; caller must check isShizukuAvailable() first.
        }
    }

    fun execute(command: String): CommandResult {
        if (!isShizukuAvailable()) return CommandResult(success = false, output = "Shizuku is not running")
        if (!isPermissionGranted()) return CommandResult(success = false, output = "Shizuku permission not granted")

        return try {
            val process = newShizukuProcess(arrayOf("sh", "-c", command))
            val stdOut = readStream(process.inputStream)
            val stdErr = readStream(process.errorStream)
            val exitCode = process.waitFor()
            CommandResult(success = exitCode == 0, output = stdOut.ifBlank { stdErr })
        } catch (throwable: Throwable) {
            CommandResult(success = false, output = throwable.message ?: "Unknown Shizuku error")
        }
    }

    private fun newShizukuProcess(command: Array<String>): Process {
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(null, command, null, null) as Process
    }

    private fun readStream(stream: InputStream): String {
        return BufferedReader(InputStreamReader(stream)).use { reader -> reader.readText().trim() }
    }

    data class CommandResult(val success: Boolean, val output: String)
}