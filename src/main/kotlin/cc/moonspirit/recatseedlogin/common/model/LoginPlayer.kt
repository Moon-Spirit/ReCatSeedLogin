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

package cc.moonspirit.recatseedlogin.common.model

import cc.moonspirit.recatseedlogin.util.Crypt

data class LoginPlayer(
    var name: String,
    var password: String,
    var email: String? = null,
    var ips: String? = null,
    var lastAction: Long = 0,
    var location: String? = null
) {
    constructor(name: String, password: String) : this(name, password, null, null, 0, null)

    override fun equals(other: Any?): Boolean = other is LoginPlayer && other.name == name
    override fun hashCode(): Int = name.hashCode()

    fun getIpsList(): List<String> = ips?.split(";") ?: emptyList()

    fun crypt() {
        password = Crypt.encrypt(name, password) ?: password
    }
}
