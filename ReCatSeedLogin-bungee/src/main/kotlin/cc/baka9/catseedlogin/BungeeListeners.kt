package cc.baka9.catseedlogin

import net.md-5.bungee.api.connection.ProxiedPlayer
import net.md-5.bungee.api.event.PlayerDisconnectEvent
import net.md-5.bungee.api.event.PostLoginEvent
import net.md-5.bungee.api.event.ChatEvent
import net.md-5.bungee.api.plugin.Listener
import java.util.concurrent.TimeUnit

class BungeeListeners : Listener {
    
    @EventHandler
    fun onPostLogin(event: PostLoginEvent) {
        val player = event.player
        val config = ReCatSeedLoginBungee.instance.configManager
        
        if (config.isEnable) {
            val lp = Database.Cache.get(player.name)
            if (lp != null && !LoginPlayerHelper.isLogin(player.name)) {
                player.sendMessage(config.i18n.get("need-login"))
                LoginPlayerHelper.addWaitingPlayer(player)
            }
        }
    }
    
    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        LoginPlayerHelper.remove(event.player.name)
    }
    
    @EventHandler
    fun onChat(event: ChatEvent) {
        if (!event.isCommand) return
        if (!LoginPlayerHelper.isWaitingPlayer(event.player)) return
        
        event.isCancelled = true
        event.player.sendMessage(ReCatSeedLoginBungee.instance.configManager.i18n.get("need-login"))
    }
}