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

package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.bukkit.config.BukkitConfigManager
import cc.moonspirit.recatseedlogin.bukkit.database.SQL
import java.util.logging.Logger

/**
 * PluginContext
 *
 * 静态单例上下文，持有 [CatSeedLogin] 插件实例、SQL 连接和 ProtocolLib 加载状态。
 *
 * 设计要点：
 * - 使用 [companion object] 模拟 Java 的静态成员。
 * - [plugin] 通过 [init] 在插件启动时初始化，使用 [lateinit] 模拟 Java 中"未初始化则抛 NPE"的语义。
 * - [sql] 是可变状态，通过 [setSql] 在重载配置时热替换数据库连接。
 */
class PluginContext private constructor() {
    companion object {
        private lateinit var plugin: CatSeedLogin
        private var loadProtocolLib: Boolean = false
        private var sqlInstance: SQL? = null

        @JvmStatic
        fun init(plugin: CatSeedLogin, sql: SQL?, loadProtocolLib: Boolean) {
            this.plugin = plugin
            this.sqlInstance = sql
            this.loadProtocolLib = loadProtocolLib
        }

        @JvmStatic
        fun get(): PluginContext? = if (::plugin.isInitialized) PluginContext() else null

        @JvmStatic
        fun getSql(): SQL? = sqlInstance

        @JvmStatic
        fun setSql(sql: SQL?) {
            this.sqlInstance = sql
            CatSeedLogin.sql = sql
        }

        @JvmStatic
        fun isLoadProtocolLib(): Boolean = loadProtocolLib

        @JvmStatic
        fun getLogger(): Logger = plugin.logger

        @JvmStatic
        fun getConfigManager(): BukkitConfigManager? = plugin.getConfigManager()

        @JvmStatic
        fun getPlugin(): CatSeedLogin = plugin
    }
}