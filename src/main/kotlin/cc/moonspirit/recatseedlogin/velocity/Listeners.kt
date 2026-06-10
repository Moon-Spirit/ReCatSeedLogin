package cc.moonspirit.recatseedlogin.velocity

import cc.moonspirit.recatseedlogin.velocity.config.VelocityConfigManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

class Listeners(
    private val configManager: VelocityConfigManager,
    private val communication: VelocityCommunication,
    private val proxyServer: ProxyServer,
    private val logger: Logger,
) {

    private val loggedInPlayerList: MutableList<String> = CopyOnWriteArrayList()

    val loggedInPlayers: List<String>
        get() = loggedInPlayerList

    @Subscribe
    fun onChat(event: PlayerChatEvent) {
        val player = event.player
        val message = event.message

        if (message.startsWith("/") && isNotLoggedIn(player)) {
            event.result = PlayerChatEvent.ChatResult.denied()
            handleLogin(player, message)
        }
    }

    @Subscribe
    fun onCommandExecute(event: CommandExecuteEvent) {
        if (event.commandSource !is Player) {
            return
        }

        val player = event.commandSource as Player
        val command = event.command
        val lower = command.lowercase()

        if (isNotLoggedIn(player) &&
            !lower.startsWith("login") &&
            !lower.startsWith("register") &&
            !lower.startsWith("l") &&
            !lower.startsWith("reg") &&
            !lower.startsWith("cslv")
        ) {
            event.result = CommandExecuteEvent.CommandResult.denied()
            handleLogin(player, "/$command")
        }
    }

    @Subscribe
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        val player = event.player
        val target = event.result.server.orElse(null) ?: return

        val playerName = player.username
        val loginServerName = configManager.getLoginServerName()

        if (loggedInPlayerList.contains(playerName)) return

        val targetName = target.serverInfo.name
        if (targetName == loginServerName) {
            handleLogin(player, null)
            return
        }

        checkLoginAsync(player, playerName, loginServerName, event)
    }

    private fun checkLoginAsync(
        player: Player,
        playerName: String,
        loginServerName: String,
        event: ServerPreConnectEvent,
    ) {
        PluginMain.runAsync { handleLoginCheck(playerName, loginServerName, event) }
    }

    private fun handleLoginCheck(playerName: String, loginServerName: String, event: ServerPreConnectEvent) {
        try {
            if (communication.sendConnectRequest(playerName) == 1) {
                loggedInPlayerList.add(playerName)
            } else {
                redirectToLoginServer(loginServerName, event)
            }
        } catch (e: Exception) {
            logger.error("Error checking login status for player: $playerName", e)
        }
    }

    private fun redirectToLoginServer(loginServerName: String, event: ServerPreConnectEvent) {
        proxyServer.getServer(loginServerName).ifPresent { loginServer ->
            event.result = ServerPreConnectEvent.ServerResult.allowed(loginServer)
        }
    }

    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        val player = event.player
        val serverName = event.server.serverInfo.name
        val loginServerName = configManager.getLoginServerName()

        if (serverName == loginServerName && loggedInPlayerList.contains(player.username)) {
            PluginMain.runAsyncDelayed({
                communication.sendKeepLoggedInRequest(player.username)
            }, 1, TimeUnit.SECONDS)
        }
    }

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        val player = event.player
        if (player != null) {
            try {
                loggedInPlayerList.remove(player.username)
            } catch (e: Exception) {
                logger.warn("Failed to remove player from logged-in list: ${player.username}")
            }
        }
    }

    @Subscribe
    fun onPreLogin(event: PreLoginEvent) {
        val playerName = event.username

        try {
            if (loggedInPlayerList.contains(playerName) && communication.sendConnectRequest(playerName) == 1) {
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text("您已经登录，请勿重复登录。")
                )
            }
        } catch (e: Exception) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(
                Component.text("发生错误，请稍后再试。")
            )
        }
    }

    private fun isNotLoggedIn(player: Player): Boolean {
        return !loggedInPlayerList.contains(player.username)
    }

    private fun handleLogin(player: Player, message: String?) {
        val playerName = player.username
        PluginMain.runAsync { handleLoginAsync(player, playerName, message) }
    }

    private fun handleLoginAsync(player: Player, playerName: String, message: String?) {
        try {
            if (communication.sendConnectRequest(playerName) != 1) return

            loggedInPlayerList.add(playerName)
            executeQueuedCommand(player, message)
        } catch (e: Exception) {
            logger.error("Error handling login for player: $playerName", e)
        }
    }

    private fun executeQueuedCommand(player: Player, message: String?) {
        if (message == null || !message.startsWith("/")) return
        proxyServer.commandManager.executeAsync(player, message.substring(1))
    }
}