package cc.baka9.catseedlogin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.geysermc.floodgate.api.FloodgateApi

class Listeners : Listener {
    
    private fun playerIsNotMinecraftPlayer(p: Player): Boolean {
        return !p.javaClass.name.matches("org\\.bukkit\\.craftbukkit.*?\\.entity\\.CraftPlayer".toRegex())
    }

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        val input = event.message.lowercase()
        for (regex in Config.Settings.commandWhiteList) {
            if (regex.matcher(input).find()) return
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerLogin(event: AsyncPlayerPreLoginEvent) {
        if (!Database.Cache.isLoaded) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "服务器还在初始化..")
            return
        }
        val name = event.name
        val lp = Database.Cache.getIgnoreCase(name)
        if (lp == null) return
        if (lp.name != name) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "游戏名字母大小写不匹配,请使用游戏名${lp.name}重新尝试登录")
            return
        }
        if (LoginPlayerHelper.isLogin(name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "玩家 ${lp.name} 已经在线了!")
            return
        }
        val hostAddress = event.address.hostAddress
        val count = Bukkit.onlinePlayers.stream()
            .filter { p -> p.address.address.hostAddress == hostAddress }
            .count()
        if (!event.address.isLoopbackAddress && count >= Config.Settings.ipCountLimit) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "太多相同ip的账号同时在线!")
        }
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (LoginPlayerHelper.isLogin(event.player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player || LoginPlayerHelper.isLogin(event.whoClicked.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (Config.Settings.beforeLoginNoDamage) {
            val entity = event.entity
            if (entity is Player && !playerIsNotMinecraftPlayer(entity) && !LoginPlayerHelper.isLogin(entity.name)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if ((Config.Settings.canTpSpawnLocation && event.to == Config.Settings.spawnLocation) ||
            playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        val from = event.from
        val to = event.to
        if (from.blockX == to.blockX && from.blockZ == to.blockZ && from.y - to.y >= 0.0) {
            return
        }
        if (Config.Settings.canTpSpawnLocation) {
            CatScheduler.teleport(player, Config.Settings.spawnLocation)
        } else {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (LoginPlayerHelper.isLogin(player.name)) {
            if (!player.isDead || Config.Settings.deathStateQuitRecordLocation) {
                Config.setOfflineLocation(player)
            }
            CatScheduler.runTaskLater({ LoginPlayerHelper.remove(player.name) }, Config.Settings.reenterInterval)
        }
        Task.getTaskAutoKick().playerJoinTime.remove(player.name)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (Config.Settings.bedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) {
            player.sendMessage(Config.Language.BEDROCK_LOGIN_BYPASS)
            return
        }
        if (Config.Settings.loginwiththesameIP && LoginPlayerHelper.recordCurrentIP(player)) {
            player.sendMessage(Config.Language.LOGIN_WITH_THE_SAME_IP)
            if (Config.Settings.afterLoginBack && Config.Settings.canTpSpawnLocation) {
                Config.getOfflineLocation(player).ifPresent { location ->
                    CatScheduler.runTaskLater({ CatScheduler.teleport(player, location) }, 1L)
                }
            }
            return
        }
        Database.Cache.refresh(player.name)
        if (Config.Settings.canTpSpawnLocation) {
            CatScheduler.teleport(player, Config.Settings.spawnLocation)
        }
    }

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val name = event.name
        if (Config.Settings.limitChineseID && !name.matches(Config.Settings.namePattern)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "请使用由数字,字母和下划线组成的游戏名,才能进入游戏")
            return
        }
        if (Config.Settings.floodgatePrefixProtect && Bukkit.pluginManager.getPlugin("floodgate") != null) {
            val prefix = FloodgateApi.getInstance().playerPrefix
            if (name.lowercase().startsWith(prefix.lowercase()) && !FloodgateApi.getInstance().isFloodgatePlayer(event.uniqueId)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "非法的基岩版玩家名称,请非基岩版玩家的名称不要以$prefix 开头")
                return
            }
        }
        when {
            name.length < Config.Settings.minLengthID -> {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "你的游戏名太短了,至少需要 ${Config.Settings.minLengthID} 个字符的长度")
            }
            name.length > Config.Settings.maxLengthID -> {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "你的游戏名太长了,最长只能到达 ${Config.Settings.maxLengthID} 个字符的长度")
            }
            else -> {}
        }
    }
}