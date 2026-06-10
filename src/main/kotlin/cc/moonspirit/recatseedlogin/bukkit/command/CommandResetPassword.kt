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
import cc.moonspirit.recatseedlogin.bukkit.objects.EmailCode
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.util.EmailSender
import cc.moonspirit.recatseedlogin.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandResetPassword : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false

        val player: Player = sender
        val name = player.name

        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true

        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) {
            sender.sendMessage(Config.Language.RESETPASSWORD_NOREGISTER)
            return true
        }
        if (!Config.EmailVerify.Enable) {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_DISABLE)
            return true
        }

        if (args[0].equals("forget", ignoreCase = true)) {
            return handleForget(sender, name, lp)
        }

        if (args[0].equals("re", ignoreCase = true) && args.size > 2) {
            return handleReset(player, lp, args[1], args[2])
        }

        return true
    }

    private fun handleForget(sender: CommandSender, name: String, lp: LoginPlayer): Boolean {
        val email = lp.email
        if (email == null) {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_NO_SET)
            return true
        }

        try {
            val optional = EmailCode.getByName(name, EmailCode.Type.ResetPassword)
            if (optional.isPresent) {
                sender.sendMessage(
                    Config.Language.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE
                        .replace("{email}", optional.get().email)
                )
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val emailCode = EmailCode.create(name, email, EMAIL_CODE_DURATION, EmailCode.Type.ResetPassword)
        sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_SENDING_MESSAGE.replace("{email}", email))

        sendResetEmailAsync(sender, name, emailCode)
        return true
    }

    private fun sendResetEmailAsync(sender: CommandSender, name: String, emailCode: EmailCode?) {
        CatScheduler.runTaskAsync(Runnable {
            try {
                if (emailCode != null) {
                    val content = buildResetEmailContent(name, emailCode)
                    EmailSender.sendEmail(emailCode.email, "重置密码", content)
                    notifyEmailSent(sender, emailCode.email)
                }
            } catch (e: Exception) {
                notifyEmailFailed(sender)
                e.printStackTrace()
            }
        })
    }

    private fun buildResetEmailContent(name: String, emailCode: EmailCode): String {
        val minutes = emailCode.durability / (1000 * 60)
        return "你的验证码是 <strong>" + emailCode.code + "</strong>" +
                "<br/>在服务器中使用帐号 " + name + " 输入指令<strong>/resetpassword re " +
                emailCode.code + " 新密码</strong> 来重置新密码" +
                "<br/>此验证码有效期为 " + minutes + "分钟"
    }

    private fun notifyEmailSent(sender: CommandSender, email: String) {
        Bukkit.getScheduler().runTask(PluginContext.getPlugin(), Runnable {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_SENT_MESSAGE.replace("{email}", email))
        })
    }

    private fun notifyEmailFailed(sender: CommandSender) {
        Bukkit.getScheduler().runTask(PluginContext.getPlugin(), Runnable {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_WARN)
        })
    }

    private fun handleReset(player: Player, lp: LoginPlayer, code: String, pwd: String): Boolean {
        val sender: CommandSender = player
        if (lp.email == null) {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_NO_SET)
            return true
        }

        try {
            val optional = EmailCode.getByName(lp.name, EmailCode.Type.ResetPassword)
            if (!optional.isPresent) {
                sender.sendMessage(Config.Language.RESETPASSWORD_FAIL)
                return true
            }
            if (optional.get().code != code) {
                sender.sendMessage(Config.Language.RESETPASSWORD_EMAILCODE_INCORRECT)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage(Config.Language.RESETPASSWORD_FAIL)
            return true
        }

        if (Util.passwordIsDifficulty(pwd)) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE)
            return true
        }

        sender.sendMessage("§e密码重置中..")
        processPasswordResetAsync(player, lp, pwd)
        return true
    }

    private fun processPasswordResetAsync(player: Player, lp: LoginPlayer, pwd: String) {
        val sender: CommandSender = player
        val name = lp.name
        CatScheduler.runTaskAsync {
            executePasswordReset(name, lp, pwd, sender)
        }
    }

    private fun executePasswordReset(name: String, lp: LoginPlayer, pwd: String, sender: CommandSender) {
        try {
            lp.password = pwd
            lp.crypt()
            PluginContext.getSql()?.edit(lp)
            Cache.refresh(name)
            LoginPlayerHelper.remove(lp)
            EmailCode.removeByName(name, EmailCode.Type.ResetPassword)
            val player: Player? = Bukkit.getPlayer(name)
            notifyResetSuccess(name, player)
        } catch (e: Exception) {
            sender.sendMessage("§c数据库异常!")
            e.printStackTrace()
        }
    }

    private fun notifyResetSuccess(name: String, player: Player?) {
        val p: Player? = Bukkit.getPlayer(name)
        if (p == null || !p.isOnline) return

        val spawn = Config.Settings.SpawnLocation
        if (Config.Settings.CanTpSpawnLocation && spawn != null) {
            CatScheduler.teleport(p, spawn)
        }
        p.sendMessage(Config.Language.RESETPASSWORD_SUCCESS)

        if (PluginContext.isLoadProtocolLib() && player != null) {
            LoginPlayerHelper.sendBlankInventoryPacket(player)
        }
    }

    companion object {
        private const val EMAIL_CODE_DURATION = 1000L * 60 * 5
    }
}