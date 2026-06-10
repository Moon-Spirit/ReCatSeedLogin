package cc.moonspirit.recatseedlogin.bungee

import cc.moonspirit.recatseedlogin.bungee.config.BungeeConfigManager
import cc.moonspirit.recatseedlogin.common.communication.BaseCommunication
import java.util.logging.Logger

class BungeeCommunication(private val configManager: BungeeConfigManager, private val logger: Logger) : BaseCommunication() {

    override fun getProxyHost(): String {
        return configManager.getProxyHost()
    }

    override fun getProxyPort(): Int {
        return configManager.getProxyPort()
    }

    override fun logError(message: String, e: Exception) {
        logger.severe(message)
        e.printStackTrace()
    }

    override fun logWarning(message: String) {
        logger.warning(message)
    }

    override fun getAuthKey(): String {
        return configManager.getAuthKey()
    }
}
