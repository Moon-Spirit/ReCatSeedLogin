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