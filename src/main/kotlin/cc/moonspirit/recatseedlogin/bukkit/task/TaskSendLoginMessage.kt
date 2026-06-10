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

package cc.moonspirit.recatseedlogin.bukkit.task

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TaskSendLoginMessage : Task() {

    override fun run() {
        if (!Cache.isLoaded) return

        for (player in Bukkit.getOnlinePlayers()) {
            try {
                val playerName = player.name
                if (!LoginPlayerHelper.isLogin(playerName)) {
                    player.sendMessage(
                        if (LoginPlayerHelper.isRegister(playerName)) Config.Language.LOGIN_REQUEST
                        else Config.Language.REGISTER_REQUEST
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}