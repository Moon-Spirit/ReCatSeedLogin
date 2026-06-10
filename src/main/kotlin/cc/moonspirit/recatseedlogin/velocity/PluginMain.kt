package cc.moonspirit.recatseedlogin.velocity

import cc.moonspirit.recatseedlogin.common.i18n.I18n
import cc.moonspirit.recatseedlogin.velocity.config.VelocityConfigManager
import cc.moonspirit.recatseedlogin.velocity.config.VelocityPlatformAdapter
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@Plugin(
    id = "catseedlogin",
    name = "CatSeedLogin",
    version = "@version@",
    description = "CatSeedLogin的Velocity适配版本，提供跨服登录验证功能",
    authors = ["shulng"],
)
class PluginMain @Inject constructor(
    val proxyServer: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path,
) {

    lateinit var configManager: VelocityConfigManager
        private set
    lateinit var platformAdapter: VelocityPlatformAdapter
        private set
    lateinit var communication: VelocityCommunication
        private set
    lateinit var listeners: Listeners
        private set

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        configManager = VelocityConfigManager(this)
        platformAdapter = VelocityPlatformAdapter(this, configManager.getI18n())
        communication = VelocityCommunication(configManager, logger)
        listeners = Listeners(configManager, communication, proxyServer, logger)
        configManager.reload()

        proxyServer.eventManager.register(this, listeners)

        proxyServer.commandManager.register(
            proxyServer.commandManager.metaBuilder("CatSeedLoginVelocity")
                .aliases("cslv")
                .build(),
            Commands(configManager, proxyServer, logger),
        )

        logger.info("CatSeedLogin-Velocity has been enabled!")
    }

    fun getI18n(): I18n {
        return configManager.getI18n()
    }

    companion object {
        lateinit var instance: PluginMain
            private set

        @JvmStatic
        fun runAsync(runnable: Runnable): ScheduledTask {
            return instance.proxyServer.scheduler
                .buildTask(instance, runnable)
                .schedule()
        }

        @JvmStatic
        fun runAsyncDelayed(runnable: Runnable, delay: Long, unit: TimeUnit): ScheduledTask {
            return instance.proxyServer.scheduler
                .buildTask(instance, runnable)
                .delay(delay, unit)
                .schedule()
        }
    }

    init {
        instance = this
    }
}