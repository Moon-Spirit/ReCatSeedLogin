package cc.baka9.catseedlogin

object LoginPlayerHelper {
    private val loggedPlayers = mutableMapOf<String, Long>()
    private val waitingPlayers = mutableSetOf<String>()
    private val exitTimeMap = mutableMapOf<String, Long>()
    private val playerIps = mutableMapOf<String, String>()

    fun isLogin(name: String): Boolean {
        val time = loggedPlayers[name] ?: return false
        if (System.currentTimeMillis() - time > 300000) {
            loggedPlayers.remove(name)
            return false
        }
        return true
    }

    fun addLoggedPlayer(name: String) {
        loggedPlayers[name] = System.currentTimeMillis()
        waitingPlayers.remove(name)
    }

    fun remove(name: String) {
        loggedPlayers.remove(name)
        waitingPlayers.remove(name)
        exitTimeMap[name] = System.currentTimeMillis()
    }

    fun addWaitingPlayer(player: net.md-5.bungee.api.connection.ProxiedPlayer) {
        waitingPlayers.add(player.name)
        playerIps[player.name] = player.address.hostString
    }

    fun isWaitingPlayer(player: net.md-5.bungee.api.connection.ProxiedPlayer): Boolean {
        return waitingPlayers.contains(player.name)
    }

    fun onPlayerQuit(name: String) {
        remove(name)
    }

    fun recordPlayerExitTime(name: String) {
        exitTimeMap[name] = System.currentTimeMillis()
    }

    fun recordCurrentIP(player: net.md-5.bungee.api.connection.ProxiedPlayer): Boolean {
        val cachedIp = playerIps[player.name]
        val currentIp = player.address.hostString
        if (cachedIp == currentIp) {
            addLoggedPlayer(player.name)
            return true
        }
        return false
    }

    fun isFloodgatePlayer(player: org.bukkit.entity.Player): Boolean {
        return try {
            val floodgateApi = Class.forName("org.geysermc.floodgate.api.FloodgateApi")
                .getMethod("getInstance")
                .invoke(null)
            val isFloodgatePlayer = floodgateApi.javaClass.getMethod("isFloodgatePlayer", java.util.UUID::class.java)
            isFloodgatePlayer.invoke(floodgateApi, player.uniqueId) as Boolean
        } catch (e: Exception) {
            false
        }
    }
}