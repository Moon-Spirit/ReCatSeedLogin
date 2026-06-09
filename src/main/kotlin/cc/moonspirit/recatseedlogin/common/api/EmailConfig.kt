package cc.moonspirit.recatseedlogin.common.api

interface EmailConfig {
    fun isEmailEnable(): Boolean
    fun getEmailAccount(): String
    fun getEmailPassword(): String
    fun getEmailSmtpHost(): String
    fun getEmailSmtpPort(): String
    fun isSSLAuthVerify(): Boolean
    fun getFromPersonal(): String
}
