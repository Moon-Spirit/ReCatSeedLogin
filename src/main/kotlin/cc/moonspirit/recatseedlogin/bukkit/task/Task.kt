package cc.moonspirit.recatseedlogin.bukkit.task

import cc.moonspirit.recatseedlogin.bukkit.CatScheduler
import cc.moonspirit.recatseedlogin.bukkit.CatSeedLogin
import space.arim.morepaperlib.scheduling.ScheduledTask
import java.util.ArrayList

abstract class Task : Runnable {

    companion object {
        private val scheduledTasks: MutableList<ScheduledTask> = ArrayList()
        private val plugin: CatSeedLogin = CatSeedLogin.instance ?: error("CatSeedLogin not initialized")
        private var taskAutoKick: TaskAutoKick? = null
        private var taskSendLoginMessage: TaskSendLoginMessage? = null

        @JvmStatic
        fun getTaskAutoKick(): TaskAutoKick {
            return taskAutoKick ?: TaskAutoKick().also { taskAutoKick = it }
        }

        @JvmStatic
        fun getTaskSendLoginMessage(): TaskSendLoginMessage {
            return taskSendLoginMessage ?: TaskSendLoginMessage().also { taskSendLoginMessage = it }
        }

        @JvmStatic
        fun runAll() {
            runTaskTimer(getTaskSendLoginMessage(), 20L * 5)
            runTaskTimer(getTaskAutoKick(), 20L * 5)
        }

        @JvmStatic
        fun cancelAll() {
            scheduledTasks.forEach { it.cancel() }
            scheduledTasks.clear()
        }

        @JvmStatic
        fun runTaskTimer(runnable: Runnable, delay: Long) {
            try {
                scheduledTasks.add(CatScheduler.runTaskTimer(runnable, 0, delay))
            } catch (e: Exception) {
                plugin.logger.severe(e.message)
            }
        }
    }
}