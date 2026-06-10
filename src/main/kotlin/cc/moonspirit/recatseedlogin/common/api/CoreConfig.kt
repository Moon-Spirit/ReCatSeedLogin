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

import java.util.regex.Pattern

interface CoreConfig {
    fun getIpRegisterCountLimit(): Int
    fun getIpCountLimit(): Int
    fun isLimitChineseID(): Boolean
    fun isBedrockLoginBypass(): Boolean
    fun isLoginWithSameIP(): Boolean
    fun isEmptyBackpack(): Boolean
    fun getIPTimeout(): Int
    fun getMaxLengthID(): Int
    fun getMinLengthID(): Int
    fun isBeforeLoginNoDamage(): Boolean
    fun getReenterInterval(): Long
    fun isAfterLoginBack(): Boolean
    fun isCanTpSpawnLocation(): Boolean
    fun getAutoKick(): Int
    fun getNamePattern(): String
    fun isDeathStateQuitRecordLocation(): Boolean
    fun isFloodgatePrefixProtect(): Boolean
    fun getCommandWhiteList(): List<Pattern>
    fun getSpawnLocation(): SpawnLocation
}

interface SpawnLocation {
    fun getWorld(): String
    fun getX(): Double
    fun getY(): Double
    fun getZ(): Double
    fun getYaw(): Float
    fun getPitch(): Float
}
