package cc.moonspirit.recatseedlogin.velocity

import cc.moonspirit.recatseedlogin.common.i18n.MessageKey
import cc.moonspirit.recatseedlogin.velocity.config.VelocityConfigManager
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture

class Commands(
    private val configManager: VelocityConfigManager,
    private val proxyServer: ProxyServer,
    private val logger: Logger,
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        val args = invocation.arguments()

        try {
            if (!source.hasPermission("catseedlogin.admin")) {
                source.sendMessage(Component.text(MessageKey.NO_PERMISSION.get() ?: MessageKey.NO_PERMISSION.getKey()))
                return
            }

            if (args.isEmpty()) {
                sendHelp(source)
                return
            }

            when (args[0].lowercase()) {
                "reload" -> handleReload(source)
                "status" -> handleStatus(source)
                "list" -> handleList(source)
                else -> sendHelp(source)
            }
        } catch (e: Exception) {
            source.sendMessage(Component.text("执行命令时发生错误", NamedTextColor.RED))
            logger.error("Error executing command", e)
        }
    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        val args = invocation.arguments()

        if (args.size <= 1) {
            return CompletableFuture.completedFuture(listOf("reload", "status", "list"))
        }

        return CompletableFuture.completedFuture(emptyList())
    }

    private fun sendHelp(source: CommandSource) {
        source.sendMessage(Component.text("=== CatSeedLogin-Velocity 命令帮助 ===", NamedTextColor.GOLD))
        source.sendMessage(Component.text("/cslv reload - 重载配置文件", NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/cslv status - 查看插件状态", NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/cslv list - 查看已登录玩家列表", NamedTextColor.YELLOW))
    }

    private fun handleReload(source: CommandSource) {
        try {
            configManager.reload()
            source.sendMessage(Component.text(MessageKey.CONFIG_RELOADED.get() ?: MessageKey.CONFIG_RELOADED.getKey()))
        } catch (e: Exception) {
            source.sendMessage(Component.text("重载配置文件时出错: " + e.message, NamedTextColor.RED))
            logger.error("Failed to reload config", e)
        }
    }

    private fun handleStatus(source: CommandSource) {
        try {
            source.sendMessage(Component.text("=== CatSeedLogin-Velocity 状态 ===", NamedTextColor.GOLD))

            val host = configManager.getProxyHost()
            val port = configManager.getProxyPort()
            val loginServerName = configManager.getLoginServerName()

            source.sendMessage(Component.text("监听地址: $host:$port", NamedTextColor.YELLOW))
            source.sendMessage(Component.text("登录服务器: $loginServerName", NamedTextColor.YELLOW))

            val loginServerOnline = proxyServer.getServer(loginServerName).isPresent

            source.sendMessage(
                Component.text(
                    "登录服务器状态: " + if (loginServerOnline) "在线" else "离线",
                    if (loginServerOnline) NamedTextColor.GREEN else NamedTextColor.RED,
                )
            )
        } catch (e: Exception) {
            source.sendMessage(Component.text("获取状态时发生错误", NamedTextColor.RED))
            logger.error("Error getting status", e)
        }
    }

    private fun handleList(source: CommandSource) {
        try {
            val listeners = PluginMain.instance.listeners
            val loggedInPlayers = listeners.loggedInPlayers

            source.sendMessage(Component.text("=== 已登录玩家列表 ===", NamedTextColor.GOLD))
            source.sendMessage(Component.text("已登录玩家数量: ${loggedInPlayers.size}", NamedTextColor.YELLOW))

            if (loggedInPlayers.isEmpty()) {
                source.sendMessage(Component.text("暂无已登录玩家", NamedTextColor.GRAY))
            } else {
                loggedInPlayers.forEach { playerName ->
                    source.sendMessage(Component.text("- $playerName", NamedTextColor.WHITE))
                }
            }
        } catch (e: Exception) {
            source.sendMessage(Component.text("获取玩家列表时发生错误", NamedTextColor.RED))
            logger.error("Error getting player list", e)
        }
    }
}