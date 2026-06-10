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
import cc.moonspirit.recatseedlogin.bukkit.PluginContext
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.util.Crypt
import cc.moonspirit.recatseedlogin.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandChangePassword : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        if (args.size != 3 || sender !is Player) return false

        val player: Player = sender
        val name = player.name

        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true

        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_NOREGISTER)
            return true
        }
        if (!LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_NOLOGIN)
            return true
        }
        val encrypted = Crypt.encrypt(name, args[0])
        if (encrypted != lp.password.trim()) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_OLDPASSWORD_INCORRECT)
            return true
        }
        if (args[1] != args[2]) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL)
            return true
        }
        if (Util.passwordIsDifficulty(args[1])) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE)
            return true
        }
        if (!Cache.isLoaded) return true

        sender.sendMessage("§e修改中..")
        changePasswordAsync(sender, player, lp, args[1])
        return true
    }

    private fun changePasswordAsync(sender: CommandSender, player: Player, lp: LoginPlayer, newPwd: String) {
        CatScheduler.runTaskAsync { executePasswordChange(sender, player, lp, newPwd) }
    }

    private fun executePasswordChange(sender: CommandSender, player: Player, lp: LoginPlayer, newPwd: String) {
        try {
            lp.password = newPwd
            lp.crypt()
            PluginContext.getSql()?.edit(lp)
            Cache.refresh(lp.name)
            LoginPlayerHelper.remove(lp)
            CatScheduler.runTask { notifyChangeSuccess(sender, player) }
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("§c服务器内部错误!")
        }
    }

    private fun notifyChangeSuccess(sender: CommandSender, player: Player) {
        val online: Player? = Bukkit.getPlayer(player.uniqueId)
        if (online == null || !online.isOnline) return

        online.sendMessage(Config.Language.CHANGEPASSWORD_SUCCESS)
        Config.setOfflineLocation(online)
        if (!Config.Settings.CanTpSpawnLocation) return

        val spawn = Config.Settings.SpawnLocation
        if (spawn != null) {
            online.teleport(spawn)
        }
        if (PluginContext.isLoadProtocolLib()) {
            LoginPlayerHelper.sendBlankInventoryPacket(online)
        }
    }
}