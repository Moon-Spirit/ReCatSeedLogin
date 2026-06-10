package cc.moonspirit.recatseedlogin.velocity

import cc.moonspirit.recatseedlogin.common.communication.BaseCommunication
import cc.moonspirit.recatseedlogin.velocity.config.VelocityConfigManager
import org.slf4j.Logger

class VelocityCommunication(
    private val configManager: VelocityConfigManager,
    private val logger: Logger,
) : BaseCommunication() {

    override fun getProxyHost(): String = configManager.getProxyHost()

    override fun getProxyPort(): Int = configManager.getProxyPort()

    override fun logError(message: String, e: Exception) {
        logger.error(message, e)
    }

    override fun logWarning(message: String) {
        logger.warn(message)
    }

    override fun getAuthKey(): String = configManager.getAuthKey()
}