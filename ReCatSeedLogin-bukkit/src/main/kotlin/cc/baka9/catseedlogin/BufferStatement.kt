package cc.baka9.catseedlogin

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

abstract class BufferStatement(private val sql: String, private vararg val args: Any?) {
    
    fun prepareStatement(connection: Connection): PreparedStatement {
        val ps = connection.prepareStatement(sql)
        for (i in args.indices) {
            args[i]?.let { ps.setObject(i + 1, it) } ?: ps.setNull(i + 1, java.sql.Types.OTHER)
        }
        return ps
    }
}