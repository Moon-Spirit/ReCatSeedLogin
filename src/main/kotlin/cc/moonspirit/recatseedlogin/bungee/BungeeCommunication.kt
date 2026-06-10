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
