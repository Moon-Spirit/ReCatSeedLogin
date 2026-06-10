package cc.moonspirit.recatseedlogin.bukkit.objects

import cc.moonspirit.recatseedlogin.bukkit.Cache
import cc.moonspirit.recatseedlogin.bukkit.CatSeedLogin
import cc.moonspirit.recatseedlogin.bukkit.Config
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.common.util.ValidationUtil
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.StructureModifier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.geysermc.floodgate.api.FloodgateApi
import java.net.InetAddress
import java.util.ArrayList
import java.util.Arrays
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

object LoginPlayerHelper {

    private val set: MutableSet<LoginPlayer> = ConcurrentHashMap.newKeySet()
    private val playerExitTimes: MutableMap<String, Long> = ConcurrentHashMap()

    @JvmStatic
    fun getList(): List<LoginPlayer> = ArrayList(set)

    @JvmStatic
    fun add(lp: LoginPlayer?) {
        if (lp == null) return
        try {
            set.add(lp)
        } catch (e: Exception) {
            CatSeedLogin.instance?.logger?.severe("Failed to add LoginPlayer to set: " + e.message)
        }
    }

    @JvmStatic
    fun remove(lp: LoginPlayer?) {
        if (lp == null) return
        try {
            set.remove(lp)
        } catch (e: Exception) {
            CatSeedLogin.instance?.logger?.severe("Failed to remove LoginPlayer from set: " + e.message)
        }
    }

    @JvmStatic
    fun remove(name: String?) {
        if (name == null) return
        try {
            set.removeIf { lp: LoginPlayer? -> lp != null && name == lp.name }
        } catch (e: Exception) {
            CatSeedLogin.instance?.logger?.severe("Failed to remove LoginPlayer by name: $name - ${e.message}")
        }
    }

    @JvmStatic
    fun isLogin(name: String): Boolean {
        return (Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name)) ||
                (Config.Settings.LoginwiththesameIP && recordCurrentIP(name)) ||
                set.any { it.name == name }
    }

    @JvmStatic
    fun isRegister(name: String): Boolean {
        return (Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name)) || Cache.getIgnoreCase(name) != null
    }

    @JvmStatic
    fun recordCurrentIP(name: String): Boolean {
        val player = Bukkit.getPlayerExact(name)
        return player != null && recordCurrentIP(player)
    }

    @JvmStatic
    fun recordCurrentIP(player: Player?): Boolean {
        if (player == null) {
            return false
        }
        val currentIP: String? = Optional.ofNullable(player.address)
            .map { addr -> addr.address }
            .map(InetAddress::getHostAddress)
            .orElse(null)
        if (currentIP == null) return false

        val storedPlayer = Cache.getIgnoreCase(player.name)
        if (storedPlayer != null) {
            val storedIPs = getStoredIPs(storedPlayer)
            val exitTime = playerExitTimes[player.name]

            if (ValidationUtil.isLoopbackAddress(currentIP)) return false
            return if (Config.Settings.IPTimeout == 0) {
                storedIPs.contains(currentIP)
            } else {
                exitTime != null && storedIPs.contains(currentIP) &&
                        (System.currentTimeMillis() - exitTime) <= Config.Settings.IPTimeout.toLong() * 60 * 1000L
            }
        }

        return false
    }

    @JvmStatic
    fun recordPlayerExitTime(playerName: String?) {
        if (playerName == null) return
        if (Config.Settings.IPTimeout != 0 && isLogin(playerName)) {
            try {
                playerExitTimes[playerName] = System.currentTimeMillis()
            } catch (e: Exception) {
                CatSeedLogin.instance?.logger?.severe("Failed to record player exit time: $playerName - ${e.message}")
            }
        }
    }

    @JvmStatic
    fun onPlayerQuit(playerName: String?) {
        recordPlayerExitTime(playerName)
    }

    @JvmStatic
    fun getStoredIPs(lp: LoginPlayer?): List<String> {
        if (lp == null || lp.ips == null) {
            return ArrayList()
        }
        return try {
            val ips = lp.ips ?: return ArrayList()
            ArrayList(Arrays.asList(*ips.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        } catch (e: Exception) {
            ArrayList()
        }
    }

    @JvmStatic
    fun isFloodgatePlayer(name: String): Boolean {
        val player = Bukkit.getPlayerExact(name)
        return player != null && isFloodgatePlayer(player)
    }

    @JvmStatic
    fun isFloodgatePlayer(player: Player?): Boolean {
        return try {
            Bukkit.getPluginManager().getPlugin("floodgate") != null &&
                    FloodgateApi.getInstance().isFloodgatePlayer(player?.uniqueId ?: return false)
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun getLastLoginTime(name: String): Long? {
        val loginPlayer = Cache.getIgnoreCase(name)
        return loginPlayer?.lastAction
    }

    @JvmStatic
    fun recordCurrentIP(player: Player?, lp: LoginPlayer) {
        try {
            val currentIp: String? = Optional.ofNullable(player?.address)
                .map { addr -> addr.address }
                .map(InetAddress::getHostAddress)
                .orElse(null)

            if (currentIp == null) {
                return
            }

            var ipsList: MutableList<String> = if (lp.getIpsList().isNotEmpty())
                ArrayList(lp.getIpsList())
            else
                ArrayList()
            ipsList = ipsList.stream().distinct().collect(Collectors.toList()) as MutableList<String>
            if (ipsList.isNotEmpty()) {
                ipsList.removeAt(ipsList.size - 1)
            }
            ipsList.add(currentIp)
            lp.ips = ipsList.joinToString(";")

            CatSeedLogin.instance?.runTaskAsync(Runnable {
                try {
                    CatSeedLogin.sql?.edit(lp)
                    Cache.refresh(lp.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
        } catch (e: Exception) {
            CatSeedLogin.instance?.logger?.warning("Failed to record IP for player: " + player?.name + " - " + e.message)
        }
    }

    @JvmStatic
    fun sendBlankInventoryPacket(player: Player) {
        if (!Config.Settings.EmptyBackpack) return

        try {
            val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
            val inventoryPacket: PacketContainer = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS)
            inventoryPacket.integers.write(0, 0)
            val blankInventory = arrayOfNulls<ItemStack>(45)
            Arrays.fill(blankInventory, ItemStack(Material.AIR))

            val itemArrayModifier = inventoryPacket.itemArrayModifier
            if (itemArrayModifier.size() > 0) {
                itemArrayModifier.write(0, blankInventory)
            } else {
                val itemListModifier = inventoryPacket.itemListModifier
                itemListModifier.write(0, Arrays.asList(*blankInventory))
            }

            protocolManager.sendServerPacket(player, inventoryPacket, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}