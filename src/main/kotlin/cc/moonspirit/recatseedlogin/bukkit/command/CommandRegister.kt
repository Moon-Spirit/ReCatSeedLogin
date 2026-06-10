package cc.moonspirit.recatseedlogin.bukkit.command

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.CatScheduler
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.bukkit.PluginContext
import cc.moonspirit.recatseedlogin.bukkit.event.CatSeedPlayerRegisterEvent
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandRegister : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        if (args.size != 2) return false
        val player: Player = sender as Player
        val name = sender.name

        if (!canRegister(player, name)) {
            return true
        }
        if (args[0] != args[1]) {
            sender.sendMessage(Config.Language.REGISTER_PASSWORD_CONFIRM_FAIL)
            return true
        }
        if (Util.passwordIsDifficulty(args[0])) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE)
            return true
        }
        if (!Cache.isLoaded) return true

        sender.sendMessage("§e注册中..")
        registerPlayerAsync(player, name, args[0])
        return true
    }

    private fun canRegister(player: Player, name: String): Boolean {
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return false
        if (LoginPlayerHelper.isLogin(name)) {
            player.sendMessage(Config.Language.REGISTER_AFTER_LOGIN_ALREADY)
            return false
        }
        if (LoginPlayerHelper.isRegister(name)) {
            player.sendMessage(Config.Language.REGISTER_BEFORE_LOGIN_ALREADY)
            return false
        }
        return true
    }

    private fun registerPlayerAsync(player: Player, name: String, password: String) {
        CatScheduler.runTaskAsync(Runnable {
            try {
                processRegistration(player, name, password)
            } catch (e: Exception) {
                e.printStackTrace()
                player.sendMessage("§c服务器内部错误!")
            }
        })
    }

    @Throws(Exception::class)
    private fun processRegistration(player: Player, name: String, password: String) {
        val playerAddress = player.address
        val currentIp: String? = if (playerAddress != null) playerAddress.address.hostAddress else null
        val loginPlayersByIp = if (currentIp != null) PluginContext.getSql()?.getLikeByIp(currentIp) ?: emptyList() else emptyList()

        if (playerAddress != null && !playerAddress.address.isLoopbackAddress
            && loginPlayersByIp.size >= Config.Settings.IpRegisterCountLimit
        ) {
            val names = loginPlayersByIp.joinToString(", ") { it.name }
            player.sendMessage(
                Config.Language.REGISTER_MORE
                    .replace("{count}", loginPlayersByIp.size.toString())
                    .replace("{accounts}", names)
            )
            return
        }

        val lp = LoginPlayer(name, password)
        lp.crypt()
        PluginContext.getSql()?.add(lp)
        Cache.refresh(lp.name)
        LoginPlayerHelper.add(lp)
        CatScheduler.runTask {
            val p = Bukkit.getPlayer(name) ?: return@runTask
            val registerEvent = CatSeedPlayerRegisterEvent(p)
            Bukkit.getServer().pluginManager.callEvent(registerEvent)
        }
        player.sendMessage(Config.Language.REGISTER_SUCCESS)
        CatScheduler.updateInventory(player)
        LoginPlayerHelper.recordCurrentIP(player, lp)
    }
}