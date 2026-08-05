package com.devbay.launcher.vault

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class VaultRepository(context: Context) {

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

    fun isVaultConfigured(): Boolean {
        return preferences.contains(KEY_PASSWORD_HASH) && preferences.contains(KEY_PASSWORD_SALT)
    }

    fun setVaultPassword(password: String) {
        val salt = VaultCrypto.generateSalt()
        val hash = VaultCrypto.hashPassword(password, salt)
        preferences.edit()
            .putString(KEY_PASSWORD_HASH, hash)
            .putString(KEY_PASSWORD_SALT, VaultCrypto.saltToString(salt))
            .apply()
    }

    fun verifyPassword(password: String): Boolean {
        val storedHash = preferences.getString(KEY_PASSWORD_HASH, null) ?: return false
        val storedSaltHex = preferences.getString(KEY_PASSWORD_SALT, null) ?: return false
        val salt = VaultCrypto.saltFromString(storedSaltHex)
        val candidateHash = VaultCrypto.hashPassword(password, salt)
        return candidateHash == storedHash
    }

    fun isBiometricEnabled(): Boolean = preferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getHiddenApps(): Set<String> {
        return preferences.getStringSet(KEY_HIDDEN_APPS, emptySet()) ?: emptySet()
    }

    fun hideApp(appKey: String) {
        val current = getHiddenApps().toMutableSet()
        current.add(appKey)
        preferences.edit().putStringSet(KEY_HIDDEN_APPS, current).apply()
    }

    fun unhideApp(appKey: String) {
        val current = getHiddenApps().toMutableSet()
        current.remove(appKey)
        preferences.edit().putStringSet(KEY_HIDDEN_APPS, current).apply()
    }

    fun isHidden(appKey: String): Boolean = getHiddenApps().contains(appKey)

    companion object {
        const val MIN_PASSWORD_LENGTH = 4
        private const val PREFS_NAME = "devbay_vault_secure"
        private const val KEY_PASSWORD_HASH = "vault_password_hash"
        private const val KEY_PASSWORD_SALT = "vault_password_salt"
        private const val KEY_BIOMETRIC_ENABLED = "vault_biometric_enabled"
        private const val KEY_HIDDEN_APPS = "vault_hidden_apps"
    }
}