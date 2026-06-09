package cc.moonspirit.recatseedlogin.bukkit.database

import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Date
import kotlin.collections.ArrayList

abstract class SQL(protected var plugin: JavaPlugin) {

    @Throws(SQLException::class)
    fun init() {
        try {
            flush(BufferStatement("CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email CHAR(255), ips CHAR(255), lastAction TIMESTAMP, location CHAR(255) DEFAULT NULL)"))
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to create accounts table: " + e.message)
            throw e
        }

        try {
            flush(BufferStatement("ALTER TABLE accounts ADD email CHAR(255)"))
        } catch (e: SQLException) {
            val msg = e.message ?: ""
            if (!msg.toLowerCase().contains("duplicate column name")) throw e
        }

        try {
            flush(BufferStatement("ALTER TABLE accounts ADD ips CHAR(255)"))
        } catch (e: SQLException) {
            val msg = e.message ?: ""
            if (!msg.toLowerCase().contains("duplicate column name")) throw e
        }

        try {
            flush(BufferStatement("ALTER TABLE accounts ADD location CHAR(255)"))
        } catch (e: SQLException) {
            val msg = e.message ?: ""
            if (!msg.toLowerCase().contains("duplicate column name")) throw e
        }
    }

    fun add(lp: LoginPlayer) {
        try {
            flush(
                BufferStatement(
                    "INSERT INTO accounts (name, password, lastAction, email, ips, location) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf(lp.name, lp.password, Date(), lp.email, lp.ips, lp.location)
                )
            )
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to add player: " + lp.name + " - " + e.message)
        }
    }

    fun del(name: String) {
        try {
            flush(BufferStatement("DELETE FROM accounts WHERE name = ?", arrayOf<Any>(name)))
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to delete player: $name - ${e.message}")
        }
    }

    fun edit(lp: LoginPlayer) {
        try {
            flush(
                BufferStatement(
                    "UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ?, location = ? WHERE name = ?",
                    arrayOf(lp.password, Date(), lp.email, lp.ips, lp.location, lp.name)
                )
            )
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to edit player: " + lp.name + " - " + e.message)
        }
    }

    fun updateLocation(name: String, location: String) {
        try {
            flush(BufferStatement("UPDATE accounts SET location = ? WHERE name = ?", arrayOf<Any>(location, name)))
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to update location for player: $name - ${e.message}")
        }
    }

    fun getLocation(name: String): String? {
        return try {
            queryForString("SELECT location FROM accounts WHERE name = ?", arrayOf<Any>(name))
        } catch (e: Exception) {
            plugin.logger.severe("Failed to get location for player: $name - ${e.message}")
            null
        }
    }

    fun get(name: String): LoginPlayer? {
        val sql = "SELECT * FROM accounts WHERE name = ?"
        try {
            BufferStatement(sql, arrayOf<Any>(name)).prepareStatement(getConnection()).use { ps ->
                ps.executeQuery().use { resultSet ->
                    return mapLoginPlayerOrNull(resultSet)
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to get player: $name")
            return null
        }
    }

    private fun queryForString(sql: String, params: Array<Any>): String? {
        try {
            BufferStatement(sql, params).prepareStatement(getConnection()).use { ps ->
                ps.executeQuery().use { resultSet ->
                    return if (resultSet.next()) resultSet.getString(1) else null
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to query: $sql - ${e.message}")
            return null
        }
    }

    @Throws(SQLException::class)
    private fun mapLoginPlayerOrNull(resultSet: ResultSet): LoginPlayer? {
        if (resultSet.next()) {
            return mapLoginPlayer(resultSet)
        }
        return null
    }

    fun getAll(): List<LoginPlayer> {
        try {
            BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection()).use { ps ->
                ps.executeQuery().use { resultSet ->
                    val lps = ArrayList<LoginPlayer>()
                    while (resultSet.next()) {
                        lps.add(mapLoginPlayer(resultSet))
                    }
                    return lps
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to get all players: " + e.message)
            return ArrayList()
        }
    }

    fun getLikeByIp(ip: String): List<LoginPlayer> {
        val likePattern = "%$ip%"
        try {
            BufferStatement("SELECT * FROM accounts WHERE ips LIKE ?", arrayOf<Any>(likePattern))
                .prepareStatement(getConnection()).use { ps ->
                    ps.executeQuery().use { resultSet ->
                        val lps = ArrayList<LoginPlayer>()
                        while (resultSet.next()) {
                            lps.add(mapLoginPlayer(resultSet))
                        }
                        return lps
                    }
                }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to query players by IP: $ip - ${e.message}")
            return ArrayList()
        }
    }

    @Throws(SQLException::class)
    private fun mapLoginPlayer(resultSet: ResultSet): LoginPlayer {
        val lp = LoginPlayer(resultSet.getString("name"), resultSet.getString("password"))
        lp.lastAction = resultSet.getTimestamp("lastAction").time
        lp.email = resultSet.getString("email")
        lp.ips = resultSet.getString("ips")
        lp.location = resultSet.getString("location")
        return lp
    }

    @Throws(SQLException::class)
    abstract fun getConnection(): Connection

    abstract fun closeConnection()

    @Throws(SQLException::class)
    fun flush(bufferStatement: BufferStatement) {
        try {
            bufferStatement.prepareStatement(getConnection()).use { ps ->
                ps.executeUpdate()
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Failed to execute flush: " + e.message)
            throw e
        }
    }
}