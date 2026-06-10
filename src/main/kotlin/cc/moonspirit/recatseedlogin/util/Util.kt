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

import cc.moonspirit.recatseedlogin.common.util.DateUtil
import cc.moonspirit.recatseedlogin.common.util.ValidationUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

object Util {
    private val passwordDifficultyRegex = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun passwordIsDifficulty(pwd: String?): Boolean {
        return ValidationUtil.isPasswordTooSimple(pwd)
    }

    fun time2Str(time: Long): String {
        synchronized(sdf) {
            return sdf.format(Date(time))
        }
    }

    fun checkMail(eMail: String?): Boolean {
        return ValidationUtil.isValidEmail(eMail)
    }

    fun randomStr(): String {
        return DateUtil.generateVerificationCode()
    }

    fun isOSLinux(): Boolean {
        return System.getProperty("os.name").lowercase().contains("linux")
    }
}
