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

package cc.moonspirit.recatseedlogin.bungee.config

import cc.moonspirit.recatseedlogin.bungee.PluginMain
import cc.moonspirit.recatseedlogin.common.Version
import cc.moonspirit.recatseedlogin.common.api.PlatformAdapter
import cc.moonspirit.recatseedlogin.common.i18n.I18n
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.TimeUnit

class BungeePlatformAdapter(private val plugin: PluginMain, private val i18n: I18n) : PlatformAdapter {

    override fun getName(): String {
        return "BungeeCord"
    }

    override fun getVersion(): String {
        return Version.VERSION
    }

    override fun logInfo(message: String) {
        plugin.logger.info(message)
    }

    override fun logWarn(message: String) {
        plugin.logger.warning(message)
    }

    override fun logError(message: String) {
        plugin.logger.severe(message)
    }

    override fun logError(message: String, throwable: Throwable) {
        plugin.logger.severe(message)
        throwable.printStackTrace()
    }

    override fun runAsync(task: Runnable) {
        PluginMain.runAsync(task)
    }

    override fun runSync(task: Runnable) {
        task.run()
    }

    override fun runAsyncLater(task: Runnable, delayTicks: Long) {
        ProxyServer.getInstance().scheduler.schedule(plugin, task, delayTicks * 50, TimeUnit.MILLISECONDS)
    }

    override fun runSyncLater(task: Runnable, delayTicks: Long) {
        runAsyncLater(task, delayTicks)
    }

    override fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        ProxyServer.getInstance().scheduler.schedule(plugin, task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS)
    }

    override fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        runAsyncTimer(task, delayTicks, periodTicks)
    }

    override fun getI18n(): I18n {
        return i18n
    }

    override fun getPlatformPlayer(name: String): Any {
        return ProxyServer.getInstance().getPlayer(name)!!
    }

    override fun isPlayerOnline(name: String): Boolean {
        val player = ProxyServer.getInstance().getPlayer(name)
        return player != null
    }

    override fun kickPlayer(name: String, reason: String) {
        val player = ProxyServer.getInstance().getPlayer(name)
        player?.disconnect(reason)
    }

    override fun sendMessage(playerName: String, message: String) {
        val player = ProxyServer.getInstance().getPlayer(playerName)
        player?.sendMessage(message)
    }

    override fun broadcast(message: String) {
        for (player in ProxyServer.getInstance().players) {
            player.sendMessage(message)
        }
    }
}
