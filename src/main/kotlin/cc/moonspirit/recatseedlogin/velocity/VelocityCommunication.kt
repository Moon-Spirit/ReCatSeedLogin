/*
 * Original work: CatSeedLogin
 * Copyright (c) 2021 CatSeed
 *
 * Licensed under the MIT License - see the LICENSE file for details.
 * (Original package: cc.baka9.catseedlogin)
 *
 * -------------------------------------------------
 * Modifications and additional code
 * Copyright (c) 2026 Yueling
 * This work is licensed under the GNU GPL v3.0-or-later
 */

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