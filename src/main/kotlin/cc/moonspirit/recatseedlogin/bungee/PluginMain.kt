package cc.moonspirit.recatseedlogin.bungee

import cc.moonspirit.recatseedlogin.bungee.config.BungeeConfigManager
import cc.moonspirit.recatseedlogin.bungee.config.BungeePlatformAdapter
import cc.moonspirit.recatseedlogin.common.i18n.I18n
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask

class PluginMain : Plugin() {
    companion object {
        lateinit var instance: PluginMain
            private set

        @JvmStatic
        fun runAsync(runnable: Runnable): ScheduledTask {
            return instance.proxy.scheduler.runAsync(instance, runnable)
        }
    }

    private lateinit var configManager: BungeeConfigManager
    private lateinit var platformAdapter: BungeePlatformAdapter
    private lateinit var communication: BungeeCommunication

    override fun onEnable() {
        instance = this
        configManager = BungeeConfigManager(this)
        platformAdapter = BungeePlatformAdapter(this, configManager.getI18n())
        communication = BungeeCommunication(configManager, logger)
        configManager.reload()
        proxy.pluginManager.registerListener(this, Listeners(configManager, communication))
        proxy.pluginManager.registerCommand(this, BungeeCommands("CatSeedLoginBungee", "catseedlogin.admin", configManager, "cslb"))
    }

    fun getConfigManager(): BungeeConfigManager {
        return configManager
    }

    fun getPlatformAdapter(): BungeePlatformAdapter {
        return platformAdapter
    }

    fun getI18n(): I18n {
        return configManager.getI18n()
    }

    fun getCommunication(): BungeeCommunication {
        return communication
    }
}
