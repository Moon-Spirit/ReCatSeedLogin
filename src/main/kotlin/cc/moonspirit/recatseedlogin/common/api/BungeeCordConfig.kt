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

package cc.moonspirit.recatseedlogin.common.api

interface BungeeCordConfig {
    fun isEnable(): Boolean
    fun getProxyHost(): String
    fun getProxyPort(): Int
    fun getAuthKey(): String
    fun getLoginServerName(): String
}
