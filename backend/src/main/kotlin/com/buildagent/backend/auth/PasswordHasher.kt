package com.buildagent.backend.auth

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    fun generateSalt(): ByteArray {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 310_000, 256)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        return Base64.getEncoder().encodeToString(key.encoded)
    }

    fun verify(password: String, saltBase64: String, storedHash: String): Boolean {
        val salt = Base64.getDecoder().decode(saltBase64)
        return hash(password, salt) == storedHash
    }
}
