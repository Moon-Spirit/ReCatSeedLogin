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

package cc.moonspirit.recatseedlogin.common.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ThreadLocalRandom

object DateUtil {

    private val DEFAULT_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private const val VERIFICATION_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private const val DEFAULT_CODE_LENGTH = 10

    @JvmStatic
    fun formatTime(timeMillis: Long): String {
        synchronized(DEFAULT_FORMAT) {
            return DEFAULT_FORMAT.format(Date(timeMillis))
        }
    }

    @JvmStatic
    fun formatTime(timeMillis: Long, pattern: String): String {
        val sdf = SimpleDateFormat(pattern)
        synchronized(sdf) {
            return sdf.format(Date(timeMillis))
        }
    }

    @JvmStatic
    fun generateVerificationCode(): String {
        return generateVerificationCode(DEFAULT_CODE_LENGTH)
    }

    @JvmStatic
    fun generateVerificationCode(length: Int): String {
        var len = length
        if (len <= 0) {
            len = DEFAULT_CODE_LENGTH
        }
        val sb = StringBuilder(len)
        val random = ThreadLocalRandom.current()
        for (i in 0 until len) {
            sb.append(VERIFICATION_CODE_CHARS[random.nextInt(VERIFICATION_CODE_CHARS.length)])
        }
        return sb.toString()
    }

    @JvmStatic
    fun isExpired(createTime: Long, durationMillis: Long): Boolean {
        return System.currentTimeMillis() - createTime > durationMillis
    }

    @JvmStatic
    fun getRemainingTime(createTime: Long, durationMillis: Long): Long {
        val elapsed = System.currentTimeMillis() - createTime
        return Math.max(0, durationMillis - elapsed)
    }
}
