package cc.moonspirit.recatseedlogin.velocity.config

import cc.moonspirit.recatseedlogin.common.Version
import cc.moonspirit.recatseedlogin.common.api.PlatformAdapter
import cc.moonspirit.recatseedlogin.common.i18n.I18n
import cc.moonspirit.recatseedlogin.velocity.PluginMain
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import java.util.concurrent.TimeUnit

class VelocityPlatformAdapter(private val plugin: PluginMain, private val i18n: I18n) : PlatformAdapter {

    override fun getName(): String = "Velocity"

    override fun getVersion(): String = Version.VERSION

    override fun logInfo(message: String) {
        plugin.logger.info(message)
    }

    override fun logWarn(message: String) {
        plugin.logger.warn(message)
    }

    override fun logError(message: String) {
        plugin.logger.error(message)
    }

    override fun logError(message: String, throwable: Throwable) {
        plugin.logger.error(message, throwable)
    }

    override fun runAsync(task: Runnable) {
        PluginMain.runAsync(task)
    }

    override fun runSync(task: Runnable) {
        task.run()
    }

    override fun runAsyncLater(task: Runnable, delayTicks: Long) {
        plugin.proxyServer.scheduler
            .buildTask(plugin, task)
            .delay(delayTicks * 50, TimeUnit.MILLISECONDS)
            .schedule()
    }

    override fun runSyncLater(task: Runnable, delayTicks: Long) {
        runAsyncLater(task, delayTicks)
    }

    override fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        plugin.proxyServer.scheduler
            .buildTask(plugin, task)
            .delay(delayTicks * 50, TimeUnit.MILLISECONDS)
            .repeat(periodTicks * 50, TimeUnit.MILLISECONDS)
            .schedule()
    }

    override fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        runAsyncTimer(task, delayTicks, periodTicks)
    }

    override fun getI18n(): I18n = i18n

    override fun getPlatformPlayer(name: String): Any {
        return plugin.proxyServer.getPlayer(name).orElse(null) as Any
    }

    override fun isPlayerOnline(name: String): Boolean {
        return plugin.proxyServer.getPlayer(name).isPresent
    }

    override fun kickPlayer(name: String, reason: String) {
        plugin.proxyServer.getPlayer(name).ifPresent { player ->
            player.disconnect(Component.text(reason))
        }
    }

    override fun sendMessage(playerName: String, message: String) {
        plugin.proxyServer.getPlayer(playerName).ifPresent { player ->
            player.sendMessage(Component.text(message))
        }
    }

    override fun broadcast(message: String) {
        val proxyServer: ProxyServer = plugin.proxyServer
        for (player in proxyServer.allPlayers) {
            player.sendMessage(Component.text(message))
        }
    }
}