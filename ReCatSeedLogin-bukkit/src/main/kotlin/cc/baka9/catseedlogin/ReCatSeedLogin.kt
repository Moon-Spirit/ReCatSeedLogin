package cc.baka9.catseedlogin

import org.bukkit.plugin.java.JavaPlugin

class ReCatSeedLogin : JavaPlugin(), Listener {
    
    companion object {
        lateinit var instance: ReCatSeedLogin
            private set
        lateinit var sql: Database.SQL
            private set
        var loadProtocolLib = false
        lateinit var morePaperLib: space.arim.morepaperlib.MorePaperLib
            private set
    }
    
    private lateinit var configManager: BukkitConfigManager
    private lateinit var platformAdapter: BukkitPlatformAdapter

    override fun onEnable() {
        instance = this
        morePaperLib = space.arim.morepaperlib.MorePaperLib(this)
        cn.handyplus.lib.adapter.HandySchedulerUtil.init(this)
        server.pluginManager.registerEvents(this, this)

        configManager = BukkitConfigManager(this)
        platformAdapter = BukkitPlatformAdapter(this, configManager.i18n)

        try {
            configManager.reload()
            Config.load()
        } catch (e: Exception) {
            e.printStackTrace()
            server.logger.warning("加载配置文件时出错，请检查你的配置文件。")
        }

        sql = if (configManager.isMySQL) Database.MySQL(this) else Database.SQLite(this)
        try {
            sql.init()
            Database.Cache.refreshAll()
        } catch (e: Exception) {
            logger.warning("§c加载数据库时出错")
            e.printStackTrace()
        }

        server.pluginManager.registerEvents(Listeners(), this)

        if (configManager.isEmptyBackpack) {
            try {
                Class.forName("com.comphenix.protocol.ProtocolLib")
                ProtocolLibListeners.enable()
                loadProtocolLib = true
            } catch (e: ClassNotFoundException) {
                logger.warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包")
            }
        }

        if (configManager.isEnable) {
            Communication.socketServerStartAsync()
        }

        if (Bukkit.pluginManager.getPlugin("floodgate") != null && configManager.isBedrockLoginBypass) {
            logger.info("检测到floodgate，基岩版兼容已装载")
        }

        registerCommands()

        Task.runAll()
    }

    private fun registerCommands() {
        server.getPluginCommand("login")?.apply {
            executor = CommandLogin()
            tabCompleter = CommandSender _, _, _, args ->
                if (args.size == 1) Collections.singletonList("密码") else ArrayList(0)
        }

        server.getPluginCommand("register")?.apply {
            executor = CommandRegister()
            tabCompleter = CommandSender _, _, _, args ->
                if (args.size == 1) Collections.singletonList("密码 重复密码") else ArrayList(0)
        }

        server.getPluginCommand("changepassword")?.apply {
            executor = CommandChangePassword()
            tabCompleter = CommandSender _, _, _, args ->
                if (args.size == 1) Collections.singletonList("旧密码 新密码 重复新密码") else ArrayList(0)
        }

        server.getPluginCommand("bindemail")?.apply {
            executor = CommandBindEmail()
            tabCompleter = CommandSender _, _, _, args -> when {
                args.size == 1 -> Arrays.asList("set 需要绑定的邮箱", "verify 邮箱验证码")
                args.size == 2 && args[0] == "set" -> Collections.singletonList("需要绑定的邮箱")
                args.size == 2 && args[0] == "verify" -> Collections.singletonList("邮箱获取的验证码")
                else -> Collections.emptyList()
            }
        }

        server.getPluginCommand("resetpassword")?.apply {
            executor = CommandResetPassword()
            tabCompleter = CommandSender _, _, _, args -> when {
                args.size == 1 -> Arrays.asList("forget", "re 验证码 新密码")
                args[0] == "re" && args.size == 2 -> Collections.singletonList("验证码 新密码")
                args[0] == "re" && args.size == 3 -> Collections.singletonList("新密码")
                else -> Collections.emptyList()
            }
        }

        server.getPluginCommand("catseedlogin")?.executor = CommandCatSeedLogin()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        LoginPlayerHelper.onPlayerQuit(event.player.name)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        CatScheduler.runTaskTimer(
            { LoginPlayerHelper.recordPlayerExitTime(event.player.name) },
            1L,
            20L
        )
    }

    override fun onDisable() {
        Task.cancelAll()
        Bukkit.onlinePlayers.forEach { p ->
            if (LoginPlayerHelper.isLogin(p.name) && (!p.isDead || configManager.isDeathStateQuitRecordLocation)) {
                Config.setOfflineLocation(p)
            }
        }

        try {
            sql.closeConnection()
        } catch (e: Exception) {
            logger.warning("关闭数据库连接时出错")
            e.printStackTrace()
        }
        Communication.socketServerStop()
        super.onDisable()
    }

    fun runTaskAsync(runnable: Runnable?) {
        runnable?.let { CatScheduler.runTaskAsync(it) }
    }

    val configManagerInstance: BukkitConfigManager get() = configManager
    val platformAdapterInstance: BukkitPlatformAdapter get() = platformAdapter
    val i18nInstance: I18n get() = configManager.i18n
}