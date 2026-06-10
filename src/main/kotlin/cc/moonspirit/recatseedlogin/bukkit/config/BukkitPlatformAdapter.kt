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

package cc.moonspirit.recatseedlogin.bukkit.config

import cc.moonspirit.recatseedlogin.bukkit.CatScheduler
import cc.moonspirit.recatseedlogin.bukkit.CatSeedLogin
import cc.moonspirit.recatseedlogin.common.Version
import cc.moonspirit.recatseedlogin.common.api.PlatformAdapter
import cc.moonspirit.recatseedlogin.common.i18n.I18n
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class BukkitPlatformAdapter(private val plugin: CatSeedLogin, private val i18n: I18n) : PlatformAdapter {

    override fun getName(): String = "Bukkit"

    override fun getVersion(): String = Version.VERSION

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
        throwable?.printStackTrace()
    }

    override fun runAsync(task: Runnable) {
        CatScheduler.runTaskAsync(task)
    }

    override fun runSync(task: Runnable) {
        CatScheduler.runTask(task)
    }

    override fun runAsyncLater(task: Runnable, delayTicks: Long) {
        CatScheduler.runTaskLaterAsync(task, delayTicks)
    }

    override fun runSyncLater(task: Runnable, delayTicks: Long) {
        CatScheduler.runTaskLater(task, delayTicks)
    }

    override fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        CatScheduler.runTaskTimerAsync(task, delayTicks, periodTicks)
    }

    override fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        CatScheduler.runTaskTimer(task, delayTicks, periodTicks)
    }

    override fun getI18n(): I18n = i18n

    override fun getPlatformPlayer(name: String): Any {
        return Bukkit.getPlayerExact(name) ?: Any()
    }

    override fun isPlayerOnline(name: String): Boolean {
        val player = Bukkit.getPlayerExact(name)
        return player != null && player.isOnline
    }

    override fun kickPlayer(name: String, reason: String) {
        val player = Bukkit.getPlayerExact(name)
        if (player != null) {
            player.kickPlayer(reason)
        }
    }

    override fun sendMessage(playerName: String, message: String) {
        val player = Bukkit.getPlayerExact(playerName)
        if (player != null) {
            player.sendMessage(message)
        }
    }

    override fun broadcast(message: String) {
        Bukkit.broadcastMessage(message)
    }
}