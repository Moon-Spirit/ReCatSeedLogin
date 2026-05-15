package cc.baka9.catseedlogin

import net.md-5.bungee.api.plugin.Plugin
import net.md-5.bungee.api.pluginListener
import net.md-5.bungee.api.plugin.PluginManager

class ReCatSeedLoginBungee : Plugin(), Listener {
    
    companion object {
        lateinit var instance: ReCatSeedLoginBungee
            private set
    }
    
    private lateinit var configManager: BungeeConfigManager
    private lateinit var platformAdapter: BungeePlatformAdapter

    override fun onEnable() {
        instance = this
        proxy.pluginManager.registerListener(this, this)
        
        configManager = BungeeConfigManager(this)
        platformAdapter = BungeePlatformAdapter(this, configManager.i18n)
        
        try {
            configManager.reload()
        } catch (e: Exception) {
            logger.warning("加载配置失败: ${e.message}")
        }
        
        proxy.pluginManager.registerListener(this, BungeeListeners())
        proxy.pluginManager.registerCommand(this, BungeeCommands())
        
        if (configManager.isEnable) {
            BungeeCommunication.start()
        }
        
        logger.info("ReCatSeedLogin 已加载!")
    }

    @Override
    fun onDisable() {
        BungeeCommunication.stop()
        super.onDisable()
    }
}