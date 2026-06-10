/*
 * Original work: CatSeedLogin
 * Copyright (c) 2021 CatSeed
 *
 * Licensed under the MIT License - see the LICENSE file for details.
 * (Original package: cc.baka9.catseedlogin)
 *
 * -------------------------------------------------
 * Modifications and additional code
 * Copyright (c) 2026 Yueling
 * This work is licensed under the GNU GPL v3.0-or-later
 */

package cc.moonspirit.recatseedlogin.bungee.config

import cc.moonspirit.recatseedlogin.bungee.PluginMain
import cc.moonspirit.recatseedlogin.common.config.BaseConfigManager
import cc.moonspirit.recatseedlogin.common.config.YamlConfiguration
import java.io.File
import java.io.InputStream

class BungeeConfigManager(private val plugin: PluginMain) : BaseConfigManager() {

    init {
        initConfig(plugin.dataFolder, "config.yml")
    }

    override fun getResource(name: String): InputStream? {
        return plugin.getResourceAsStream(name)
    }

    override fun getConfig(name: String): YamlConfiguration {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(getDataFolder(), fileName)
        val config = YamlConfiguration.loadConfiguration(file)

        val resourcePath = fileName
        try {
            val defaultStream = getResource(resourcePath)
            if (defaultStream != null) {
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
        val file = File(getDataFolder(), fileName)
        if (!file.exists()) {
            try {
                val `in` = getResource(fileName)
                if (`in` != null) {
                    java.nio.file.Files.copy(`in`, file.toPath())
                }
            } catch (e: Exception) {
                plugin.logger.warning("无法创建默认配置文件: " + e.message)
            }
        }
    }

    override fun saveConfig(name: String?) {
        if (name == null) return
        val config = getConfig(name)
        if (config != null) {
            try {
                config.save()
            } catch (e: Exception) {
                plugin.logger.warning("保存配置文件失败: " + e.message)
            }
        }
    }

    private fun mergeDefaults(config: YamlConfiguration, defaults: YamlConfiguration) {
        for (entry in defaults.getDataMap().entries) {
            if (!config.contains(entry.key)) {
                config.set(entry.key, entry.value)
            }
        }
    }
}
