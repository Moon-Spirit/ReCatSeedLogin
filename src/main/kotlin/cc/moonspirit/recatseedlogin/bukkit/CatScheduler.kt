package cc.moonspirit.recatseedlogin.bukkit

import org.bukkit.Location
import org.bukkit.entity.Player
import space.arim.morepaperlib.MorePaperLib
import space.arim.morepaperlib.scheduling.ScheduledTask
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.Duration

/**
 * CatScheduler
 *
 * MorePaperLib / HandyScheduler 的薄封装，对外暴露统一的静态调度 API。
 * 根据运行环境（Paper / Folia）自动选择合适的调度器实现。
 */
class CatScheduler {
    companion object {
        private lateinit var morePaperLib: MorePaperLib
        private var folia: Boolean = false
        private var teleportAsync: Method? = null

        @JvmStatic
        fun init(mpl: MorePaperLib) {
            morePaperLib = mpl
            folia = mpl.scheduling().isUsingFolia
            teleportAsync = initTeleportAsync()
        }

        @JvmStatic
        fun teleport(player: Player, location: Location?) {
            if (location == null || location.world == null) return
            if (!folia) {
                player.teleport(location)
                return
            }
            morePaperLib.scheduling().entitySpecificScheduler(player).run(Runnable {
                try {
                    val method = teleportAsync
                    if (method != null) {
                        method.invoke(player, location)
                    }
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException(e)
                }
            }, null)
        }

        @JvmStatic
        fun updateInventory(player: Player) {
            morePaperLib.scheduling().entitySpecificScheduler(player).run(Runnable { player.updateInventory() }, null)
        }

        @JvmStatic
        fun runTaskAsync(runnable: Runnable): ScheduledTask {
            return morePaperLib.scheduling().asyncScheduler().run(runnable)
        }

        @JvmStatic
        fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): ScheduledTask {
            return morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(
                runnable,
                if (delay == 0L) 1 else delay,
                period
            )
        }

        @JvmStatic
        fun runTask(runnable: Runnable): ScheduledTask {
            return morePaperLib.scheduling().globalRegionalScheduler().run(runnable)
        }

        @JvmStatic
        fun runTaskLater(runnable: Runnable, delay: Long): ScheduledTask {
            return morePaperLib.scheduling().globalRegionalScheduler().runDelayed(runnable, delay)
        }

        @JvmStatic
        fun runTaskLaterAsync(runnable: Runnable, delay: Long): ScheduledTask {
            return morePaperLib.scheduling().asyncScheduler().runDelayed(runnable, Duration.ofMillis(delay * 50))
        }

        @JvmStatic
        fun runTaskTimerAsync(runnable: Runnable, delay: Long, period: Long): ScheduledTask {
            return morePaperLib.scheduling().asyncScheduler().runAtFixedRate(
                runnable,
                Duration.ofMillis(delay * 50),
                Duration.ofMillis(period * 50)
            )
        }

        private fun initTeleportAsync(): Method? {
            if (folia) {
                return try {
                    Player::class.java.getMethod("teleportAsync", Location::class.java)
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException(e)
                }
            }
            return null
        }
    }
}