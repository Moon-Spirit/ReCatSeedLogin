package cc.moonspirit.recatseedlogin.common.config

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

abstract class ConfigManager(protected val dataFolder: File) {

    protected val configs: MutableMap<String, YamlConfiguration> = ConcurrentHashMap()
    protected val defaultValues: MutableMap<String, Any> = HashMap()

    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
    }

    fun getConfig(name: String): YamlConfiguration =
        configs.computeIfAbsent(name, ::loadConfig)

    protected fun loadConfig(name: String): YamlConfiguration {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(dataFolder, fileName)
        val config = YamlConfiguration.loadConfiguration(file)

        val resourcePath = fileName
        try {
            getResource(resourcePath)?.use { defaultStream ->
                val defaultConfig = YamlConfiguration(null)
                defaultConfig.loadFromResource(defaultStream)
                mergeDefaults(config, defaultConfig)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return config
    }

    protected abstract fun getResource(name: String): InputStream?

    private fun mergeDefaults(config: YamlConfiguration, defaults: YamlConfiguration) {
        for ((key, value) in defaults.getDataMap()) {
            if (!config.contains(key)) {
                config.set(key, value)
            }
        }
    }

    fun reloadConfig(name: String?) {
        if (name == null) return
        try {
            configs.remove(name)
            getConfig(name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reloadAll() {
        for (name in HashMap(configs).keys) {
            if (name != null) {
                reloadConfig(name)
            }
        }
    }

    fun getDataFolder(): File = dataFolder

    fun createDefaultConfig(name: String?) {
        if (name == null) return
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            try {
                getResource(fileName)?.use { input ->
                    Files.copy(input, file.toPath())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun saveConfig(name: String?) {
        if (name == null) return
        val config = configs[name]
        if (config != null) {
            try {
                config.save()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}