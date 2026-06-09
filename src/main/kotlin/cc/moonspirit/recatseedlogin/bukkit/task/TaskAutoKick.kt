package cc.moonspirit.recatseedlogin.bukkit.task

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.bukkit.object.LoginPlayerHelper
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class TaskAutoKick : Task() {

    val playerJoinTime: MutableMap<String, Long> = ConcurrentHashMap()

    override fun run() {
        if (!Cache.isLoaded || Config.Settings.AutoKick < 1) return

        val autoKickMs = Config.Settings.AutoKick.toLong() * 1000L
        val now = System.currentTimeMillis()

        for (player in Bukkit.getOnlinePlayers()) {
            checkAndKickPlayer(player, now, autoKickMs)
        }
    }

    private fun checkAndKickPlayer(player: Player, now: Long, autoKickMs: Long) {
        val playerName = player.name
        try {
            if (LoginPlayerHelper.isLogin(playerName)) {
                playerJoinTime.remove(playerName)
                return
            }

            playerJoinTime.putIfAbsent(playerName, now)
            val joinTime = playerJoinTime[playerName]
            if (joinTime != null && now - joinTime > autoKickMs) {
                if (!player.isOnline) {
                    playerJoinTime.remove(playerName)
                    return
                }
                val kickMessage = Config.Language.AUTO_KICK
                    .replace("{time}", Config.Settings.AutoKick.toString())
                player.kickPlayer(kickMessage)
            }
        } catch (e: Exception) {
            playerJoinTime.remove(playerName)
            e.printStackTrace()
        }
    }
}