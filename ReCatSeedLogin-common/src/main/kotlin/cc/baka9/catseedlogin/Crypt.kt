package cc.baka9.catseedlogin

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Crypt {
    private val CRYPTCHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    fun encrypt(name: String, password: String): String? {
        val text = "ÜÄaeut//&/=I $password 7421€547$name __+IÄIH§%NK $password"
        return try {
            val md = MessageDigest.getInstance("SHA-512")
            md.update(text.toByteArray(StandardCharsets.UTF_8))
            byteArrayToHexString(md.digest())
        } catch (e: Exception) {
            null
        }
    }

    private fun byteArrayToHexString(args: ByteArray): String {
        val chars = CharArray(args.size * 2)
        for (i in args.indices) {
            chars[i * 2] = CRYPTCHARS[args[i].toInt() shr 4 and 0xF]
            chars[i * 2 + 1] = CRYPTCHARS[args[i].toInt() and 0xF]
        }
        return String(chars)
    }

    fun match(name: String, password: String, encrypted: String?): Boolean {
        return try {
            encrypted != null && encrypted == encrypt(name, password)
        } catch (e: Exception) {
            false
        }
    }
}