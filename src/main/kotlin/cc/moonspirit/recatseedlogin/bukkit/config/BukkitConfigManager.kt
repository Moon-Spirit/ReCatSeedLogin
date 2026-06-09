package cc.moonspirit.recatseedlogin.bukkit.config

import cc.moonspirit.recatseedlogin.common.api.SpawnLocation
import cc.moonspirit.recatseedlogin.common.config.BaseConfigManager
import cc.moonspirit.recatseedlogin.common.config.ConfigConstants
import cc.moonspirit.recatseedlogin.common.config.YamlConfiguration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStream

class BukkitConfigManager(private val plugin: JavaPlugin) : BaseConfigManager() {

    init {
        initConfig(plugin.dataFolder, "config.yml")
    }

    override fun getResource(name: String): InputStream? {
        return plugin.getResource(name)
    }

    override fun getConfig(name: String): YamlConfiguration {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val folder = dataFolder ?: error("dataFolder not initialized")
        val file = File(folder, fileName)
        val config = YamlConfiguration.loadConfiguration(file)

        val resourcePath = fileName
        try {
            getResource(resourcePath)?.use { defaultStream ->
                val defaultConfig = YamlConfiguration(null)
                defaultConfig.loadFromResource(defaultStream)
                mergeDefaults(config, defaultConfig)
            }
        } catch (e: Exception) {
            plugin.logger.warning("无法加载默认配置文件: " + e.message)
        }

        return config
    }

    override fun createDefaultConfig(name: String?) {
        if (name == null) return
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val folder = dataFolder ?: return
        val file = File(folder, fileName)
        if (!file.exists()) {
            try {
                getResource(fileName)?.use { input ->
                    java.nio.file.Files.copy(input, file.toPath())
                }
            } catch (e: Exception) {
                plugin.logger.warning("无法创建默认配置文件: " + e.message)
            }
        }
    }

    override fun saveConfig(name: String?) {
        if (name == null) return
        val config = getConfig(name)
        try {
            config.save()
        } catch (e: Exception) {
            plugin.logger.warning("保存配置文件失败: " + e.message)
        }
    }

    private fun mergeDefaults(config: YamlConfiguration, defaults: YamlConfiguration) {
        for ((key, value) in defaults.dataMap) {
            if (!config.contains(key)) {
                config.set(key, value)
            }
        }
    }

    fun setSpawnLocation(location: Location) {
        val locStr = String.format(
            "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
            location.world.name,
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch
        )
        mainConfig?.set(ConfigConstants.Path.SPAWN_LOCATION, locStr)
        saveConfig("config.yml")
    }

    fun getBukkitSpawnLocation(): Location {
        val spawn: SpawnLocation = getSpawnLocation()
        var world: World? = Bukkit.getWorld(spawn.world)
        if (world == null) {
            world = Bukkit.getWorlds()[0]
        }
        return Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch)
    }

    val dataFolderFile: File?
        get() = dataFolder
}