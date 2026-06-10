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

package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.bukkit.task.Task
import cc.moonspirit.recatseedlogin.bukkit.task.TaskAutoKick
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
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
        for (regex in Config.Settings.CommandWhiteList) {
            if (regex.matcher(input).find()) return
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerLogin(event: AsyncPlayerPreLoginEvent) {
        if (!Cache.isLoaded) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "服务器还在初始化..")
            return
        }
        val name = event.name
        val lp: LoginPlayer? = Cache.getIgnoreCase(name)
        if (lp == null) return
        if (lp.name != name) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "游戏名字母大小写不匹配,请使用游戏名" + lp.name + "重新尝试登录"
            )
            return
        }
        if (LoginPlayerHelper.isLogin(name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "玩家 " + lp.name + " 已经在线了!")
            return
        }
        val hostAddress = event.address.hostAddress
        val count = Bukkit.getOnlinePlayers().stream()
            .filter { p -> p.address?.address?.hostAddress == hostAddress }
            .count()
        if (!event.address.isLoopbackAddress && count >= Config.Settings.IpCountLimit) {
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

    //登陆之前不能攻击
    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    //登陆之前不会受到伤害
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (Config.Settings.BeforeLoginNoDamage) {
            val entity = event.entity
            if (entity is Player && !playerIsNotMinecraftPlayer(entity) && !LoginPlayerHelper.isLogin(entity.name)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if ((Config.Settings.CanTpSpawnLocation && event.to == Config.Settings.SpawnLocation) ||
            playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)
        ) return
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
        val from: Location = event.from
        val to: Location = event.to ?: return
        if (from.blockX == to.blockX && from.blockZ == to.blockZ && from.y - to.y >= 0.0) {
            return
        }
        if (Config.Settings.CanTpSpawnLocation) {
            val spawn = Config.Settings.SpawnLocation
            if (spawn != null) {
                CatScheduler.teleport(player, spawn)
            }
        } else {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (LoginPlayerHelper.isLogin(player.name)) {
            saveOfflineLocation(player)
            CatScheduler.runTaskLater({
                try {
                    LoginPlayerHelper.remove(player.name)
                } catch (e: Exception) {
                    player.server.logger.warning("Failed to remove player on quit: " + player.name)
                }
            }, Config.Settings.ReenterInterval)
        }
        try {
            val task: TaskAutoKick? = Task.getTaskAutoKick()
            if (task != null) {
                task.playerJoinTime.remove(player.name)
            }
        } catch (e: Exception) {
            player.server.logger.warning("Failed to remove player from auto-kick list: " + player.name)
        }
    }

    private fun saveOfflineLocation(player: Player) {
        try {
            if (!player.isDead || Config.Settings.DeathStateQuitRecordLocation) {
                Config.setOfflineLocation(player)
            }
        } catch (e: Exception) {
            player.server.logger.warning("保存玩家离线位置失败: " + player.name)
        }
    }

    private fun safeRemovePlayerFromTask(playerName: String) {
        try {
            val task: TaskAutoKick? = Task.getTaskAutoKick()
            if (task != null) {
                task.playerJoinTime.remove(playerName)
            }
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to remove player from auto-kick list: $playerName")
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) {
            player.sendMessage(Config.Language.BEDROCK_LOGIN_BYPASS)
            return
        }
        if (Config.Settings.LoginwiththesameIP && LoginPlayerHelper.recordCurrentIP(player)) {
            player.sendMessage(Config.Language.LOGIN_WITH_THE_SAME_IP)
            teleportToLastLocation(player)
            return
        }
        Cache.refresh(player.name)
        if (Config.Settings.CanTpSpawnLocation) {
            val spawn = Config.Settings.SpawnLocation
            if (spawn != null) {
                CatScheduler.teleport(player, spawn)
            }
        }
    }

    private fun teleportToLastLocation(player: Player) {
        if (!Config.Settings.AfterLoginBack || !Config.Settings.CanTpSpawnLocation) return
        val location = Config.getOfflineLocation(player)
        if (location != null) {
            CatScheduler.runTaskLater({ CatScheduler.teleport(player, location) }, 1L)
        }
    }

    //id只能下划线字母数字
    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val name = event.name
        if (Config.Settings.LimitChineseID && !name.matches(Regex(Config.Settings.NamePattern))) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "请使用由数字,字母和下划线组成的游戏名,才能进入游戏")
            return
        }
        if (checkFloodgatePrefixProtect(event, name)) return
        if (name.length < Config.Settings.MinLengthID) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "你的游戏名太短了,至少需要 " + Config.Settings.MinLengthID + " 个字符的长度"
            )
        } else if (name.length > Config.Settings.MaxLengthID) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                "你的游戏名太长了,最长只能到达 " + Config.Settings.MaxLengthID + " 个字符的长度"
            )
        }
    }

    private fun checkFloodgatePrefixProtect(event: AsyncPlayerPreLoginEvent, name: String): Boolean {
        if (!Config.Settings.FloodgatePrefixProtect || Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            return false
        }
        try {
            val prefix = FloodgateApi.getInstance().playerPrefix
            if (name.lowercase().startsWith(prefix.lowercase()) &&
                !FloodgateApi.getInstance().isFloodgatePlayer(event.uniqueId)
            ) {
                event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "非法的基岩版玩家名称,请非基岩版玩家的名称不要以" + prefix + "开头"
                )
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}