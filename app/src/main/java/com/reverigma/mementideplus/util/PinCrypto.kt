package com.reverigma.mementideplus.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * PIN 安全存储：明文 PIN 永不落盘，只存「盐 + SHA-256(盐+PIN)」的哈希。
 * 校验时重新计算哈希比对，攻击者拿到文件也无法反推 PIN（盐使彩虹表失效）。
 */
object PinCrypto {
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest((pin + salt).toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
