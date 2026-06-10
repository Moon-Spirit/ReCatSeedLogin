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

import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Pattern

object ValidationUtil {

    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    )

    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$"
    )

    private val IP_ADDRESS_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )

    @JvmStatic
    fun isValidEmail(email: String?): Boolean {
        if (email == null || email.isEmpty()) {
            return false
        }
        return EMAIL_PATTERN.matcher(email).matches()
    }

    @JvmStatic
    fun isValidPassword(password: String?): Boolean {
        if (password == null || password.isEmpty()) {
            return false
        }
        return PASSWORD_PATTERN.matcher(password).matches()
    }

    @JvmStatic
    fun isPasswordTooSimple(password: String?): Boolean {
        return !isValidPassword(password)
    }

    @JvmStatic
    fun isValidIpAddress(ip: String?): Boolean {
        if (ip == null || ip.isEmpty()) {
            return false
        }
        return IP_ADDRESS_PATTERN.matcher(ip).matches()
    }

    @JvmStatic
    fun isLoopbackAddress(ip: String?): Boolean {
        if (ip == null) {
            return false
        }
        return try {
            val address = InetAddress.getByName(ip)
            address.isLoopbackAddress
        } catch (e: UnknownHostException) {
            false
        }
    }

    @JvmStatic
    fun isPrivateAddress(ip: String?): Boolean {
        if (ip == null) {
            return false
        }
        return try {
            val address = InetAddress.getByName(ip)
            address.isSiteLocalAddress || address.isLinkLocalAddress
        } catch (e: UnknownHostException) {
            false
        }
    }

    @JvmStatic
    fun sanitizeIpAddress(ip: String?): String? {
        if (ip == null) {
            return null
        }
        return ip.trim().replace(Regex("[^0-9a-fA-F.:]"), "")
    }

    @JvmStatic
    fun isValidPlayerName(name: String?): Boolean {
        if (name == null || name.isEmpty()) {
            return false
        }
        return name.matches(Regex("^\\w+$")) && name.length >= 2 && name.length <= 16
    }
}
