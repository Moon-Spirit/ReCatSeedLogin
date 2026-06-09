package cc.moonspirit.recatseedlogin.bukkit.database

import cc.moonspirit.recatseedlogin.bukkit.Config
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class MySQL(javaPlugin: JavaPlugin) : SQL(javaPlugin) {

    private var connection: Connection? = null

    @Synchronized
    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        if (isConnectionValid()) {
            return requireNotNull(connection) { "Connection should be valid here" }
        }
        closeConnection()
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + Config.MySQL.Host + ":" + Config.MySQL.Port + "/" + Config.MySQL.Database + "?characterEncoding=UTF-8",
                Config.MySQL.User, Config.MySQL.Password
            )
            return requireNotNull(this.connection) { "Connection should be set after DriverManager.getConnection" }
        } catch (e: ClassNotFoundException) {
            throw SQLException(e)
        } catch (e: SQLException) {
            throw SQLException(e)
        }
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

    override fun closeConnection() {
        try {
            val conn = connection
            if (conn != null && !conn.isClosed) {
                conn.close()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        this.connection = null
    }
}