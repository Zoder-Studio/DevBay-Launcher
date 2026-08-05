package com.devbay.launcher.vault

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object VaultCrypto {

    private const val ITERATION_COUNT = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun saltToString(salt: ByteArray): String = salt.joinToString("") { "%02x".format(it) }

    fun saltFromString(saltHex: String): ByteArray {
        return ByteArray(saltHex.length / 2) { i ->
            saltHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}