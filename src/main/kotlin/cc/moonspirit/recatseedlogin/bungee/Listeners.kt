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
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.concurrent.CopyOnWriteArrayList

class Listeners(private val configManager: BungeeConfigManager, private val communication: BungeeCommunication) : Listener {

    private val proxyServer = ProxyServer.getInstance()
    private val loggedInPlayerList = CopyOnWriteArrayList<String>()

    @EventHandler
    fun onChat(event: ChatEvent) {
        if (!event.isProxyCommand || event.sender !is ProxiedPlayer) {
            return
        }
        val player = event.sender as ProxiedPlayer
        val playerName = player.name
        if (!loggedInPlayerList.contains(playerName)) {
            event.isCancelled = true
            handleLogin(player, event.message)
        }
    }

    @EventHandler
    fun onServerConnect(event: ServerConnectEvent) {
        if (event.isCancelled) {
            return
        }
        val loginServerName = configManager.getLoginServerName()
        if (event.target.name == loginServerName) {
            return
        }
        val player = event.player
        val playerName = player.name
        if (loggedInPlayerList.contains(playerName)) {
            return
        }
        PluginMain.runAsync { checkLoginAndRedirect(player, playerName, event, loginServerName) }
    }

    private fun checkLoginAndRedirect(player: ProxiedPlayer, playerName: String, event: ServerConnectEvent, loginServerName: String) {
        try {
            if (communication.sendConnectRequest(playerName) == 1) {
                loggedInPlayerList.add(playerName)
            } else {
                event.target = proxyServer.getServerInfo(loginServerName)
            }
        } catch (e: Exception) {
            proxyServer.logger.severe("Error checking login status for player: $playerName")
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onServerConnected(event: ServerConnectedEvent) {
        val loginServerName = configManager.getLoginServerName()
        if (event.server.info.name != loginServerName) {
            return
        }
        val player = event.player
        if (loggedInPlayerList.contains(player.name)) {
            PluginMain.runAsync { communication.sendKeepLoggedInRequest(player.name) }
        }
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        try {
            loggedInPlayerList.remove(event.player.name)
        } catch (e: Exception) {
            proxyServer.logger.severe("移除玩家时出错: ${e.message}")
        }
    }

    @EventHandler
    fun onPreLogin(event: PreLoginEvent) {
        val playerName = event.connection.name
        try {
            if (loggedInPlayerList.contains(playerName) && communication.sendConnectRequest(playerName) == 1) {
                event.setCancelReason(*TextComponent.fromLegacyText("您已经登录，请勿重复登录。"))
                event.isCancelled = true
            }
        } catch (e: Exception) {
            event.setCancelReason(*TextComponent.fromLegacyText("发生错误，请稍后再试。"))
            event.isCancelled = true
        }
    }

    private fun handleLogin(player: ProxiedPlayer, message: String?) {
        val playerName = player.name
        PluginMain.runAsync {
            if (communication.sendConnectRequest(playerName) != 1) {
                return@runAsync
            }
            loggedInPlayerList.add(playerName)
            if (!message.isNullOrEmpty()) {
                proxyServer.pluginManager.dispatchCommand(player, message.substring(1))
            }
        }
    }
}
