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

package cc.moonspirit.recatseedlogin.bukkit.command

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.CatScheduler
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.bukkit.event.CatSeedPlayerLoginEvent
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.util.Crypt
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandLogin : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false
        val player: Player = sender
        val name = player.name
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true
        if (LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.LOGIN_REPEAT)
            return true
        }
        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) {
            sender.sendMessage(Config.Language.LOGIN_NOREGISTER)
            return true
        }
        val encrypted = Crypt.encrypt(name, args[0])
        if (encrypted != lp.password.trim()) {
            handleLoginFail(sender, player, lp)
            return true
        }
        handleLoginSuccess(player, lp)
        return true
    }

    private fun handleLoginSuccess(player: Player, lp: LoginPlayer) {
        LoginPlayerHelper.add(lp)
        val loginEvent = CatSeedPlayerLoginEvent(player, lp.email, CatSeedPlayerLoginEvent.Result.SUCCESS)
        Bukkit.getServer().pluginManager.callEvent(loginEvent)
        player.sendMessage(Config.Language.LOGIN_SUCCESS)
        CatScheduler.updateInventory(player)
        LoginPlayerHelper.recordCurrentIP(player, lp)
        if (Config.Settings.AfterLoginBack && Config.Settings.CanTpSpawnLocation) {
            val offlineLocation = Config.getOfflineLocation(player)
            if (offlineLocation != null) {
                CatScheduler.teleport(player, offlineLocation)
            }
        }
    }

    private fun handleLoginFail(sender: CommandSender, player: Player, lp: LoginPlayer) {
        sender.sendMessage(Config.Language.LOGIN_FAIL)
        val loginEvent = CatSeedPlayerLoginEvent(player, lp.email, CatSeedPlayerLoginEvent.Result.FAIL)
        Bukkit.getServer().pluginManager.callEvent(loginEvent)
        if (Config.EmailVerify.Enable) {
            sender.sendMessage(Config.Language.LOGIN_FAIL_IF_FORGET)
        }
    }
}