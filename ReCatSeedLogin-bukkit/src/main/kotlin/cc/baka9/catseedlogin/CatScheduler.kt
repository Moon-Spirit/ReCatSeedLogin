package cc.baka9.catseedlogin

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import cn.handyplus.lib.adapter.HandySchedulerUtil

class CatScheduler {
    
    companion object {
        private var lastId = 0
        
        fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): BukkitTask {
            return HandySchedulerUtil.runTaskTimer(ReCatSeedLogin.instance, runnable, delay, period)
        }
        
        fun runTaskLater(runnable: Runnable, delay: Long): BukkitTask {
            return HandySchedulerUtil.runTaskLater(ReCatSeedLogin.instance, runnable, delay)
        }
        
        fun runTaskAsync(runnable: Runnable): BukkitTask {
            return HandySchedulerUtil.runTaskAsync(ReCatSeedLogin.instance, runnable)
        }
        
        fun runTask(runnable: Runnable): BukkitTask {
            return HandySchedulerUtil.runTask(ReCatSeedLogin.instance, runnable)
        }
        
        fun teleport(player: org.bukkit.entity.Player, location: org.bukkit.Location?) {
            if (location != null) {
                val craftPlayer = player.javaClass.getMethod("getHandle").invoke(player)
                val entityPlayer = craftPlayer.javaClass.getField("playerConnection").get(craftPlayer)
                val server = entityPlayer.javaClass.getField("server").get(entityPlayer)
                val worldServer = entityPlayer.javaClass.getField("level").get(entityPlayer)
                
                val packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket")
                val teleportConstructor = packetClass.getConstructor(Int::class.java, 
                    net.minecraft.core.BlockPos::class.java,
                    org.bukkit.util.Vector::class.java.getDeclaredField("x").type,
                    org.bukkit.util.Vector::class.java.getDeclaredField("y").type,
                    org.bukkit.util.Vector::class.java.getDeclaredField("z").type,
                    Byte::class.java,
                    Byte::class.java,
                    Boolean::class.java
                )
                
                val x = location.x
                val y = location.y
                val z = location.z
                val yaw = (location.yaw * 256.0f / 360.0f).toInt()
                val pitch = (location.pitch * 256.0f / 360.0f).toInt()
                val isDismountNeeded = false
                val entityId = player.entityId
                
                val teleportPacket = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket")
                    .getConstructor(Int::class.java, net.minecraft.core.BlockPos::class.java, 
                        kotlin.Byte::class.java, kotlin.Byte::class.java, Boolean::class.java)
                    .newInstance(entityId, net.minecraft.core.BlockPos(x.toLong(), y.toLong(), z.toLong()), 
                        yaw.toByte(), pitch.toByte(), isDismountNeeded)
                
                entityPlayer.javaClass.getMethod("send", Class.forName("net.minecraft.network.protocol.Packet"))
                    .invoke(entityPlayer, teleportPacket)
                
                player.teleport(location)
            }
        }
    }
}