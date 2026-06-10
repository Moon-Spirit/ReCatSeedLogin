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
import cc.moonspirit.recatseedlogin.common.i18n.MessageKey
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent

class BungeeCommands(name: String, permission: String, private val configManager: BungeeConfigManager, vararg aliases: String) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases) {

    override fun execute(commandSender: CommandSender, args: Array<out String>) {
        if (commandSender == null || args == null || args.isEmpty()) {
            return
        }
        try {
            if (args[0]?.equals("reload", ignoreCase = true) == true) {
                configManager.reload()
                commandSender.sendMessage(TextComponent(MessageKey.CONFIG_RELOADED.get()))
            }
        } catch (e: Exception) {
            commandSender.sendMessage(TextComponent("§c指令执行时出错: " + e.message))
        }
    }
}
