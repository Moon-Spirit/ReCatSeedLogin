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
