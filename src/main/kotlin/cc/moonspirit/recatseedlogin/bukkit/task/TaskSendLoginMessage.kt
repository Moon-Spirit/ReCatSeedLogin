package cc.moonspirit.recatseedlogin.bukkit.task

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.bukkit.object.LoginPlayerHelper
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