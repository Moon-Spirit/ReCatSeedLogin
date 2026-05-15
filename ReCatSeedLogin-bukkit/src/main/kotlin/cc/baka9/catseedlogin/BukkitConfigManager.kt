package cc.baka9.catseedlogin

import java.io.File
import java.io.InputStream
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import cc.baka9.catseedlogin.common.config.ConfigConstants
import cc.baka9.catseedlogin.common.config.YamlConfiguration
import cc.baka9.catseedlogin.common.api.CoreConfig

class BukkitConfigManager(private val plugin: ReCatSeedLogin) : BaseConfigManager() {
    
    override fun getResource(name: String): InputStream {
        return plugin.getResource(name)
    }
    
    override fun getConfig(name: String): YamlConfiguration {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(dataFolder, fileName)
        val config = YamlConfiguration.loadConfiguration(file)
        
        try {
            getResource(fileName)?.use { defaultStream ->
                if (defaultStream != null) {
                    val defaultConfig = YamlConfiguration(null)
                    defaultConfig.loadFromResource(defaultStream)
                    mergeDefaults(config, defaultConfig)
                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("无法加载默认配置文件: ${e.message}")
        }
        
        return config
    }
    
    override fun createDefaultConfig(name: String) {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            try {
                getResource(fileName)?.use { inputStream ->
                    java.nio.file.Files.copy(inputStream, file.toPath())
                }
            } catch (e: Exception) {
                plugin.logger.warning("无法创建默认配置文件: ${e.message}")
            }
        }
    }
    
    override fun saveConfig(name: String) {
        val config = getConfig(name)
        if (config != null) {
            try {
                config.save()
            } catch (e: Exception) {
                plugin.logger.warning("保存配置文件失败: ${e.message}")
            }
        }
    }
    
    private fun mergeDefaults(config: YamlConfiguration, defaults: YamlConfiguration) {
        for (entry in defaults.dataMap.entries) {
            if (!config.contains(entry.key)) {
                config.set(entry.key, entry.value)
            }
        }
    }
    
    fun setSpawnLocation(location: Location) {
        val locStr = String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f",
            location.world.name,
            location.x, location.y, location.z,
            location.yaw, location.pitch)
        mainConfig.set(ConfigConstants.Path.SPAWN_LOCATION, locStr)
        saveConfig("config.yml")
    }
    
    fun getBukkitSpawnLocation(): Location {
        val spawn = getSpawnLocation()
        var world: World? = Bukkit.getWorld(spawn.world)
        if (world == null) {
            world = Bukkit.worlds.firstOrNull()
        }
        return Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch)
    }
}