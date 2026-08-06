package com.devbay.launcher.clipboard

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ClipboardRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getHistory(): List<ClipboardEntry> {
        val raw = preferences.getString(KEY_HISTORY, "") ?: ""
        if (raw.isBlank()) return emptyList()

        return raw.split(RECORD_SEPARATOR).mapNotNull { record ->
            val parts = record.split(FIELD_SEPARATOR)
            if (parts.size != 2) return@mapNotNull null
            val text = try {
                String(Base64.decode(parts[0], Base64.NO_WRAP))
            } catch (throwable: Throwable) {
                return@mapNotNull null
            }
            val timestamp = parts[1].toLongOrNull() ?: return@mapNotNull null
            ClipboardEntry(text, timestamp)
        }
    }

    fun addEntry(text: String) {
        if (text.isBlank()) return
        val current = getHistory().toMutableList()
        if (current.isNotEmpty() && current.first().text == text) return

        current.removeAll { it.text == text }
        current.add(0, ClipboardEntry(text, System.currentTimeMillis()))
        while (current.size > MAX_ENTRIES) current.removeAt(current.size - 1)
        saveHistory(current)
    }

    fun removeEntry(entry: ClipboardEntry) {
        val current = getHistory().toMutableList()
        current.removeAll { it.timestamp == entry.timestamp && it.text == entry.text }
        saveHistory(current)
    }

    fun clearAll() {
        saveHistory(emptyList())
    }

    private fun saveHistory(entries: List<ClipboardEntry>) {
        val encoded = entries.joinToString(RECORD_SEPARATOR) { entry ->
            val encodedText = Base64.encodeToString(entry.text.toByteArray(), Base64.NO_WRAP)
            "$encodedText$FIELD_SEPARATOR${entry.timestamp}"
        }
        preferences.edit().putString(KEY_HISTORY, encoded).apply()
    }

    companion object {
        const val MAX_ENTRIES = 50
        private const val PREFS_NAME = "devbay_clipboard_secure"
        private const val KEY_HISTORY = "clipboard_history"
        private const val FIELD_SEPARATOR = "\u0001"
        private const val RECORD_SEPARATOR = "\u0002"
    }
}