package cc.baka9.catseedlogin

import java.sql.Connection

class MySQL(private val plugin: Any) : Database.SQL(), BaseDatabaseConnection() {
    
    private val database: String
    private val username: String
    private val password: String
    private val address: String

    init {
        val config = ReCatSeedLogin.instance?.configManager?.mainConfig
        address = config?.getString("mysql.address", "localhost:3306") ?: "localhost:3306"
        database = config?.getString("mysql.database", "catseedlogin") ?: "catseedlogin"
        username = config?.getString("mysql.username", "root") ?: "root"
        password = config?.getString("mysql.password", "") ?: ""
    }

    @Throws(java.sql.SQLException::class)
    override fun createConnection(): Connection {
        val parts = address.split(":")
        val host = parts.getOrElse(0) { "localhost" }
        val port = parts.getOrElse(1) { "3306" }.toIntOrNull() ?: 3306
        
        val url = "jdbc:mysql://$host:$port/$database?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        val driver = org.sqlite.JDBC::class.java
        return java.sql.DriverManager.getConnection(url, username, password)
    }

    override fun closeConnection() {
        connection?.close()
    }

    @Throws(Exception::class)
    override fun init() {
        flush(BufferStatement("CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email CHAR(255), ips CHAR(255), lastAction TIMESTAMP, location CHAR(255) DEFAULT NULL)"))
        
        try { flush(BufferStatement("ALTER TABLE accounts ADD email CHAR(255)")) } 
        catch (e: Exception) { if (!e.message.lowercase().contains("duplicate")) throw e }
        
        try { flush(BufferStatement("ALTER TABLE accounts ADD ips CHAR(255)")) }
        catch (e: Exception) { if (!e.message.lowercase().contains("duplicate")) throw e }
        
        try { flush(BufferStatement("ALTER TABLE accounts ADD location CHAR(255)")) }
        catch (e: Exception) { if (!e.message.lowercase().contains("duplicate")) throw e }
    }

    @Throws(Exception::class)
    override fun add(lp: LoginPlayer) {
        flush(BufferStatement("INSERT INTO accounts (name, password, lastAction, email, ips, location) VALUES (?, ?, ?, ?, ?, ?)",
            lp.name, lp.password, java.util.Date(), lp.email, lp.ips, lp.location))
        Database.Cache.refresh(lp.name)
    }

    @Throws(Exception::class)
    override fun del(name: String) {
        flush(BufferStatement("DELETE FROM accounts WHERE name = ?", name))
        Database.Cache.refresh(name)
    }

    @Throws(Exception::class)
    override fun edit(lp: LoginPlayer) {
        flush(BufferStatement("UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ?, location = ? WHERE name = ?",
            lp.password, java.util.Date(), lp.email, lp.ips, lp.location, lp.name))
        Database.Cache.refresh(lp.name)
    }

    @Throws(Exception::class)
    override fun get(name: String): LoginPlayer? {
        BufferStatement("SELECT * FROM accounts WHERE name = ?", name).prepareStatement(getConnection()).use { ps ->
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    val lp = LoginPlayer(name, rs.getString("password"))
                    lp.lastAction = rs.getTimestamp("lastAction").time
                    lp.email = rs.getString("email")
                    lp.ips = rs.getString("ips")
                    lp.location = rs.getString("location")
                    return lp
                }
            }
        }
        return null
    }

    @Throws(Exception::class)
    override fun getAll(): List<LoginPlayer> {
        val players = mutableListOf<LoginPlayer>()
        BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection()).use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val lp = LoginPlayer(rs.getString("name"), rs.getString("password"))
                    lp.lastAction = rs.getTimestamp("lastAction").time
                    lp.email = rs.getString("email")
                    lp.ips = rs.getString("ips")
                    lp.location = rs.getString("location")
                    players.add(lp)
                }
            }
        }
        return players
    }
}

class SQLite(private val plugin: Any) : Database.SQL() {
    private var connection: Connection? = null
    private val dbFile: java.io.File

    init {
        val dataFolder = ReCatSeedLogin.instance?.dataFolder ?: java.io.File("plugins/CatSeedLogin")
        if (!dataFolder.exists()) dataFolder.mkdirs()
        dbFile = java.io.File(dataFolder, "CatSeedLogin.db")
    }

    @Throws(java.sql.SQLException::class)
    private fun getConnection(): Connection {
        if (connection == null || connection!!.isClosed) {
            connection = org.sqlite.JDBC.createConnection("jdbc:sqlite:${dbFile.absolutePath}")
        }
        return connection!!
    }

    override fun closeConnection() {
        connection?.close()
    }

    @Throws(Exception::class)
    override fun init() {
        flush(BufferStatement("CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email CHAR(255), ips CHAR(255), lastAction TIMESTAMP, location CHAR(255) DEFAULT NULL)"))
    }

    @Throws(Exception::class)
    override fun add(lp: LoginPlayer) {
        flush(BufferStatement("INSERT INTO accounts (name, password, lastAction, email, ips, location) VALUES (?, ?, ?, ?, ?, ?)",
            lp.name, lp.password, java.util.Date(), lp.email, lp.ips, lp.location))
        Database.Cache.refresh(lp.name)
    }

    @Throws(Exception::class)
    override fun del(name: String) {
        flush(BufferStatement("DELETE FROM accounts WHERE name = ?", name))
        Database.Cache.refresh(name)
    }

    @Throws(Exception::class)
    override fun edit(lp: LoginPlayer) {
        flush(BufferStatement("UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ?, location = ? WHERE name = ?",
            lp.password, java.util.Date(), lp.email, lp.ips, lp.location, lp.name))
        Database.Cache.refresh(lp.name)
    }

    @Throws(Exception::class)
    override fun get(name: String): LoginPlayer? {
        BufferStatement("SELECT * FROM accounts WHERE name = ?", name).prepareStatement(getConnection()).use { ps ->
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    val lp = LoginPlayer(name, rs.getString("password"))
                    lp.lastAction = rs.getTimestamp("lastAction").time
                    lp.email = rs.getString("email")
                    lp.ips = rs.getString("ips")
                    lp.location = rs.getString("location")
                    return lp
                }
            }
        }
        return null
    }

    @Throws(Exception::class)
    override fun getAll(): List<LoginPlayer> {
        val players = mutableListOf<LoginPlayer>()
        BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection()).use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val lp = LoginPlayer(rs.getString("name"), rs.getString("password"))
                    lp.lastAction = rs.getTimestamp("lastAction").time
                    lp.email = rs.getString("email")
                    lp.ips = rs.getString("ips")
                    lp.location = rs.getString("location")
                    players.add(lp)
                }
            }
        }
        return players
    }
}