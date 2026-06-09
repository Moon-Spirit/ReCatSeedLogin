package cc.moonspirit.recatseedlogin.common.api

interface DatabaseConfig {
    fun isMySQL(): Boolean
    fun getDatabaseHost(): String
    fun getDatabasePort(): Int
    fun getDatabaseName(): String
    fun getDatabaseUser(): String
    fun getDatabasePassword(): String
}
