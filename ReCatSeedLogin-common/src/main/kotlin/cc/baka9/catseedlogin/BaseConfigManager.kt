package cc.baka9.catseedlogin

import java.io.File
import java.io.InputStream

class BaseConfigManager {
    protected var dataFolder: File = File("plugins/CatSeedLogin")
    protected var mainConfig: YamlConfiguration? = null
    protected lateinit var i18n: I18n
    protected var isMySQL = false
    protected var isEnable = false
    
    fun initConfig(dataFolder: File, name: String) {
        this.dataFolder = dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()
        mainConfig = getConfig(name)
    }
    
    open fun getResource(name: String): InputStream? = null
    
    open fun getConfig(name: String): YamlConfiguration {
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
            // Ignore
        }
        
        return config
    }
    
    private fun mergeDefaults(config: YamlConfiguration, defaults: YamlConfiguration) {
        for (entry in defaults.dataMap.entries) {
            if (!config.contains(entry.key)) {
                config.set(entry.key, entry.value)
            }
        }
    }
    
    open fun createDefaultConfig(name: String) {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(dataFolder, fileName)
        if (!file.exists()) {
            try {
                getResource(fileName)?.use { inputStream ->
                    java.nio.file.Files.copy(inputStream, file.toPath())
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    open fun saveConfig(name: String) {
        mainConfig?.let {
            try {
                it.save()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    open fun reload() {
        mainConfig = getConfig("config.yml")
        
        isMySQL = mainConfig?.getBoolean("mysql.enable", false) ?: false
        isEnable = mainConfig?.getBoolean("Communication.enable", false) ?: false
        
        val lang = mainConfig?.getString("language", "zh-CN") ?: "zh-CN"
        i18n = I18n(lang)
        I18n.setInstance(i18n)
    }
}