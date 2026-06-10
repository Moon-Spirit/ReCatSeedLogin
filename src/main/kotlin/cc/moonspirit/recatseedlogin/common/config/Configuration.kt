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

package cc.moonspirit.recatseedlogin.common.config

interface Configuration {

    fun getBoolean(path: String, defaultValue: Boolean): Boolean

    fun getInt(path: String, defaultValue: Int): Int

    fun getLong(path: String, defaultValue: Long): Long

    fun getString(path: String, defaultValue: String): String

    fun getDouble(path: String, defaultValue: Double): Double

    fun getStringList(path: String): List<String>

    fun getSection(path: String): ConfigurationSection

    fun set(path: String, value: Any?)

    fun contains(path: String): Boolean

    fun getBoolean(path: String): Boolean = getBoolean(path, false)

    fun getInt(path: String): Int = getInt(path, 0)

    fun getLong(path: String): Long = getLong(path, 0L)

    fun getString(path: String): String = getString(path, "")

    fun getDouble(path: String): Double = getDouble(path, 0.0)
}