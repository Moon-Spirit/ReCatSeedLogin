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

object ConfigHelper {

    fun parseLocationString(location: LocationData): String =
        String.format(
            "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
            location.world,
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch
        )

    fun parseLocationString(locationStr: String?, defaultLocation: LocationData): LocationData {
        if (locationStr.isNullOrEmpty()) {
            return defaultLocation
        }
        val parts = locationStr.split(":")
        return try {
            LocationData(
                world = if (parts.size > 0) parts[0] else defaultLocation.world,
                x = if (parts.size > 1) parts[1].toDouble() else defaultLocation.x,
                y = if (parts.size > 2) parts[2].toDouble() else defaultLocation.y,
                z = if (parts.size > 3) parts[3].toDouble() else defaultLocation.z,
                yaw = if (parts.size > 4) parts[4].toFloat() else defaultLocation.yaw,
                pitch = if (parts.size > 5) parts[5].toFloat() else defaultLocation.pitch
            )
        } catch (e: NumberFormatException) {
            defaultLocation
        }
    }

    data class LocationData(
        var world: String,
        var x: Double,
        var y: Double,
        var z: Double,
        var yaw: Float = 0.0f,
        var pitch: Float = 0.0f
    ) {
        constructor(world: String, x: Double, y: Double, z: Double) :
            this(world, x, y, z, 0.0f, 0.0f)
    }

    fun sanitizeString(input: String?): String? = input?.trim()

    fun parseIntOrDefault(value: String?, defaultValue: Int): Int =
        parseValue(value, { it.toInt() }, defaultValue)

    fun parseLongOrDefault(value: String?, defaultValue: Long): Long =
        parseValue(value, { it.toLong() }, defaultValue)

    fun parseBooleanOrDefault(value: String?, defaultValue: Boolean): Boolean {
        if (value.isNullOrEmpty()) {
            return defaultValue
        }
        return value.toBoolean()
    }

    private fun <T> parseValue(value: String?, parser: (String) -> T, defaultValue: T): T {
        if (value.isNullOrEmpty()) {
            return defaultValue
        }
        return try {
            parser(value)
        } catch (e: Exception) {
            defaultValue
        }
    }
}