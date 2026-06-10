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

import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class SQLite(javaPlugin: JavaPlugin) : SQL(javaPlugin) {

    private var connection: Connection? = null

    @Synchronized
    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        if (isConnectionValid()) {
            return requireNotNull(connection) { "Connection should be valid here" }
        }
        closeConnection()
        connection = createConnection()
        return requireNotNull(connection) { "Connection should be set after createConnection" }
    }

    @Throws(SQLException::class)
    private fun isConnectionValid(): Boolean {
        val conn = connection ?: return false
        if (conn.isClosed) return false
        try {
            conn.prepareStatement("SELECT 1").use { ps: PreparedStatement ->
                ps.executeQuery()
            }
            return true
        } catch (e: SQLException) {
            return false
        }
    }

    @Throws(SQLException::class)
    private fun createConnection(): Connection {
        try {
            ensureDataFolderExists()
            Class.forName("org.sqlite.JDBC")
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.dataFolder.absolutePath + "/accounts.db")
        } catch (e: ClassNotFoundException) {
            throw SQLException(e)
        }
    }

    private fun ensureDataFolderExists() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
    }

    override fun closeConnection() {
        try {
            val conn = connection
            if (conn != null && !conn.isClosed) {
                conn.close()
            }
        } catch (e: SQLException) {
            plugin.logger.warning("关闭SQLite连接时出错: " + e.message)
        }
        connection = null
    }
}