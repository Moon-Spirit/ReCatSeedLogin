/*
 * Original work: CatSeedLogin
 * Copyright (c) 2021 CatSeed
 *
 * Licensed under the MIT License - see the LICENSE file for details.
 * (Original package: cc.baka9.catseedlogin)
 *
 * -------------------------------------------------
 * Modifications and additional code
 * Copyright (c) 2026 Yueling
 * This work is licensed under the GNU GPL v3.0-or-later
 */

package cc.moonspirit.recatseedlogin.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object CommunicationAuth {
    private val messageDigest: MessageDigest = try {
        MessageDigest.getInstance("SHA-256")
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException(e)
    }

    fun encryption(vararg args: String): String {
        val paramString = args.joinToString("")
        val arrayOfByte = messageDigest.digest(paramString.toByteArray(StandardCharsets.UTF_8))
        val stringBuilder = StringBuilder(arrayOfByte.size * 2)
        for (value in arrayOfByte) {
            stringBuilder.append(String.format("%02x", value.toInt() and 0xff))
        }
        return stringBuilder.toString()
    }
}
