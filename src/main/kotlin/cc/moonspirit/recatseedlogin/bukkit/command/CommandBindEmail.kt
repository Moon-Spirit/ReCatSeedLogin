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

class CommandBindEmail : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false

        val name = sender.name

        if (!canBindEmail(sender, name)) return true

        val subCommand = args[0].lowercase()
        if (subCommand == "set") {
            handleSet(sender, name, args)
        } else if (subCommand == "verify") {
            handleVerify(sender, name, args)
        }

        return true
    }

    private fun canBindEmail(sender: CommandSender, name: String): Boolean {
        val player: Player = sender as Player

        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return false

        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) {
            sender.sendMessage("§c你还未注册!")
            return false
        }
        if (!LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage("§c你还未登陆!")
            return false
        }
        if (!Config.EmailVerify.Enable) {
            sender.sendMessage("§c服务器没有开启邮箱功能")
            return false
        }
        return true
    }

    private fun handleSet(sender: CommandSender, name: String, args: Array<String>) {
        if (args.size <= 1) return

        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) return
        if (lp.email != null && Util.checkMail(lp.email)) {
            sender.sendMessage("§c你已经绑定过邮箱了!")
            return
        }

        val mail = args[1]
        if (!Util.checkMail(mail)) {
            sender.sendMessage("§c邮箱格式不正确!")
            return
        }

        try {
            val existingCode = EmailCode.getByName(name, EmailCode.Type.Bind)
            if (existingCode.isPresent && existingCode.get().email == mail) {
                sender.sendMessage("§c已经向 $mail 邮箱中发送验证码，请不要重复此操作")
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val bindEmail = EmailCode.create(name, mail, 1000L * 60 * 5, EmailCode.Type.Bind)
        sender.sendMessage("§6向邮箱发送验证码中...")
        sendEmailCode(sender, name, mail, bindEmail)
    }

    private fun handleVerify(sender: CommandSender, name: String, args: Array<String>) {
        if (args.size <= 1) return

        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) return
        if (lp.email != null && Util.checkMail(lp.email)) {
            sender.sendMessage("§c你已经绑定过邮箱了!")
            return
        }

        val emailOptional = EmailCode.getByName(name, EmailCode.Type.Bind)
        if (!emailOptional.isPresent) {
            sender.sendMessage("§c你没有待绑定的邮箱，或者验证码已过期")
            return
        }

        val bindEmail = emailOptional.get()
        if (bindEmail.code != args[1]) {
            sender.sendMessage("§c验证码错误!")
            return
        }

        sender.sendMessage("§e绑定邮箱中..")
        bindEmail(sender, lp, bindEmail)
    }

    private fun sendEmailCode(sender: CommandSender, name: String, mail: String, bindEmail: EmailCode?) {
        CatScheduler.runTaskAsync(Runnable {
            try {
                if (bindEmail != null) {
                    val content = buildBindEmailContent(name, bindEmail)
                    EmailSender.sendEmail(mail, "邮箱绑定", content)
                    notifyBindEmailSent(sender, mail)
                }
            } catch (e: Exception) {
                notifyBindEmailFailed(sender)
                e.printStackTrace()
            }
        })
    }

    private fun buildBindEmailContent(name: String, bindEmail: EmailCode): String {
        val minutes = bindEmail.durability / (1000 * 60)
        return "你的验证码是 <strong>" + bindEmail.code + "</strong>" +
                "<br/>在服务器中使用帐号 " + name + " 输入指令<strong>/bindemail verify " + bindEmail.code + "</strong> 来绑定邮箱" +
                "<br/>绑定邮箱之后可用于忘记密码时重置自己的密码" +
                "<br/>此验证码有效期为 " + minutes + "分钟"
    }

    private fun notifyBindEmailSent(sender: CommandSender, mail: String) {
        CatScheduler.runTask(Runnable {
            sender.sendMessage("§6已经向邮箱 $mail 发送了一串绑定验证码，请检查你的邮箱的收件箱")
            sender.sendMessage("§c如果未收到，请检查邮箱的垃圾箱!")
        })
    }

    private fun notifyBindEmailFailed(sender: CommandSender) {
        CatScheduler.runTask { sender.sendMessage("§c发送邮件失败,服务器内部错误!") }
    }

    private fun bindEmail(sender: CommandSender, lp: LoginPlayer, bindEmail: EmailCode) {
        CatScheduler.runTaskAsync { executeBindEmail(sender, lp, bindEmail) }
    }

    private fun executeBindEmail(sender: CommandSender, lp: LoginPlayer, bindEmail: EmailCode) {
        try {
            lp.email = bindEmail.email
            PluginContext.getSql()?.edit(lp)
            Cache.refresh(lp.name)
            notifyBindSuccess(sender, bindEmail)
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("§c服务器内部错误!")
        }
    }

    private fun notifyBindSuccess(sender: CommandSender, bindEmail: EmailCode) {
        val syncPlayer: Player? = Bukkit.getPlayer((sender as Player).uniqueId)
        if (syncPlayer == null || !syncPlayer.isOnline) return

        syncPlayer.sendMessage("§a邮箱已绑定 ${bindEmail.email} 忘记密码时可以用邮箱重置自己的密码")
        EmailCode.removeByName(syncPlayer.name, EmailCode.Type.Bind)
    }
}