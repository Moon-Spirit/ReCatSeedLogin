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

package cc.moonspirit.recatseedlogin.common.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

abstract class BaseDatabaseConnection {
    @get:JvmName("getConn")
    var connection: Connection? = null

    protected fun isConnectionValid(): Boolean {
        return try {
            val conn = connection
            if (conn == null || conn.isClosed) {
                return false
            }
            conn.prepareStatement("SELECT 1").use { ps ->
                ps.executeQuery()
            }
            true
        } catch (e: SQLException) {
            false
        }
    }

    protected fun getValidConnection(): Connection? {
        return try {
            if (isConnectionValid()) {
                return connection
            }
            closeConnection()
            connection = createConnection()
            connection
        } catch (e: SQLException) {
            null
        }
    }

    protected abstract fun createConnection(): Connection?

    abstract fun closeConnection()

    @Throws(SQLException::class)
    fun getConnection(): Connection? {
        return getValidConnection()
    }
}
