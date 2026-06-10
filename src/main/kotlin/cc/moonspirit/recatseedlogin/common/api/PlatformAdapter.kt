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

package cc.moonspirit.recatseedlogin.common.api

import cc.moonspirit.recatseedlogin.common.i18n.I18n

interface PlatformAdapter {
    fun getName(): String
    fun getVersion(): String
    fun logInfo(message: String)
    fun logWarn(message: String)
    fun logError(message: String)
    fun logError(message: String, throwable: Throwable)
    fun runAsync(task: Runnable)
    fun runSync(task: Runnable)
    fun runAsyncLater(task: Runnable, delayTicks: Long)
    fun runSyncLater(task: Runnable, delayTicks: Long)
    fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long)
    fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long)
    fun getI18n(): I18n
    fun getPlatformPlayer(name: String): Any
    fun isPlayerOnline(name: String): Boolean
    fun kickPlayer(name: String, reason: String)
    fun sendMessage(playerName: String, message: String)
    fun broadcast(message: String)
}
