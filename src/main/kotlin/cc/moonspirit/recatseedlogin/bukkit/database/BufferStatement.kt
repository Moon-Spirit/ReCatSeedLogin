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

package cc.moonspirit.recatseedlogin.bukkit.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.Arrays

class BufferStatement(private val query: String, private val values: Array<Any>) {
    private val stacktrace: Exception = Exception()

    @Throws(SQLException::class)
    fun prepareStatement(con: Connection): PreparedStatement {
        val ps = con.prepareStatement(query)
        for (i in values.indices) {
            ps.setObject(i + 1, values[i])
        }
        return ps
    }

    fun getStackTrace(): Array<StackTraceElement> = stacktrace.stackTrace

    override fun toString(): String {
        return "Query: $query, values: ${Arrays.toString(values)}"
    }
}