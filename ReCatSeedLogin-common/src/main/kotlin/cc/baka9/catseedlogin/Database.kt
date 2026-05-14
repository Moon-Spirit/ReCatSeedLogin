package cc.baka9.catseedlogin

import java.sql.Connection

abstract class BaseDatabaseConnection {
    protected var connection: Connection? = null

    @Throws(java.sql.SQLException::class)
    protected fun isConnectionValid(): Boolean {
        if (connection == null || connection!!.isClosed) {
            return false
        }
        try {
            connection!!.prepareStatement("SELECT 1").use { ps ->
                ps.executeQuery()
                return true
            }
        } catch (e: java.sql.SQLException) {
            return false
        }
    }

    @Throws(java.sql.SQLException::class)
    protected fun getValidConnection(): Connection {
        if (isConnectionValid()) {
            return connection!!
        }
        closeConnection()
        connection = createConnection()
        return connection!!
    }

    protected abstract fun createConnection(): Connection

    abstract fun closeConnection()

    @Throws(java.sql.SQLException::class)
    fun getConnection(): Connection {
        return getValidConnection()
    }
}

object Database {
    lateinit var sql: SQL
        private set

    class Cache private constructor() {
        companion object {
            private val cache = mutableMapOf<String, LoginPlayer>()
            var isLoaded = false
                private set

            fun refresh(name: String) {
                try {
                    val lp = sql.get(name)
                    if (lp != null) {
                        cache[name.lowercase()] = lp
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            fun refreshAll() {
                try {
                    cache.clear()
                    sql.getAll().forEach { lp ->
                        cache[lp.name.lowercase()] = lp
                    }
                    isLoaded = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            fun get(name: String): LoginPlayer? {
                return cache[name.lowercase()]
            }

            fun getIgnoreCase(name: String): LoginPlayer? {
                return cache[name.lowercase()]
            }
        }
    }

    abstract class SQL {
        abstract fun init()
        abstract fun add(lp: LoginPlayer)
        abstract fun del(name: String)
        abstract fun edit(lp: LoginPlayer)
        abstract fun get(name: String): LoginPlayer?
        abstract fun getAll(): List<LoginPlayer>
        abstract fun closeConnection()
        
        @Throws(Exception::class)
        fun flush(bufferStatement: BufferStatement) {
            bufferStatement.prepareStatement(getConnection()).use { ps ->
                ps.executeUpdate()
            }
        }
    }
}