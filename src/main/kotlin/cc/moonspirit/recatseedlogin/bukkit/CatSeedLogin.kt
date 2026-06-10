package cc.moonspirit.recatseedlogin.bukkit

import java.util.Collections
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import cc.moonspirit.recatseedlogin.bukkit.command.CommandBindEmail
import cc.moonspirit.recatseedlogin.bukkit.command.CommandCatSeedLogin
import cc.moonspirit.recatseedlogin.bukkit.command.CommandChangePassword
import cc.moonspirit.recatseedlogin.bukkit.command.CommandLogin
import cc.moonspirit.recatseedlogin.bukkit.command.CommandRegister
import cc.moonspirit.recatseedlogin.bukkit.command.CommandResetPassword
import cc.moonspirit.recatseedlogin.bukkit.config.BukkitConfigManager
import cc.moonspirit.recatseedlogin.bukkit.config.BukkitPlatformAdapter
import cc.moonspirit.recatseedlogin.bukkit.database.MySQL
import cc.moonspirit.recatseedlogin.bukkit.database.SQL
import cc.moonspirit.recatseedlogin.bukkit.database.SQLite
import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.bukkit.task.Task
import cc.moonspirit.recatseedlogin.common.i18n.I18n
import cn.handyplus.lib.adapter.HandySchedulerUtil
import space.arim.morepaperlib.MorePaperLib

class CatSeedLogin : JavaPlugin(), Listener {

    companion object {
        @JvmField
        var instance: CatSeedLogin? = null

        @JvmField
        var sql: SQL? = null

        @JvmField
        var loadProtocolLib: Boolean = false

        @JvmField
        var morePaperLib: MorePaperLib? = null
    }

    internal var configManager: BukkitConfigManager? = null
    internal var platformAdapter: BukkitPlatformAdapter? = null

    override fun onEnable() {
        instance = this
        val mpl = MorePaperLib(this)
        morePaperLib = mpl
        CatScheduler.init(mpl)
        HandySchedulerUtil.init(this)
        server.pluginManager.registerEvents(this, this)

        val cm = BukkitConfigManager(this)
        configManager = cm
        platformAdapter = BukkitPlatformAdapter(this, cm.getI18n())

        try {
            cm.reload()
            Config.load()
        } catch (e: Exception) {
            e.printStackTrace()
            server.logger.warning("加载配置文件时出错，请检查你的配置文件。")
        }

        sql = if (cm.isMySQL()) MySQL(this) else SQLite(this)
        try {
            sql!!.init()
            Cache.refreshAll()
        } catch (e: Exception) {
            logger.warning("§c加载数据库时出错")
            e.printStackTrace()
        }

        server.pluginManager.registerEvents(Listeners(), this)

        if (cm.isEmptyBackpack()) {
            try {
                Class.forName("com.comphenix.protocol.ProtocolLib")
                ProtocolLibListeners.enable()
                loadProtocolLib = true
            } catch (e: ClassNotFoundException) {
                logger.warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包")
            }
        }

        if (cm.isEnable()) {
            Communication.socketServerStartAsync()
        }

        if (Bukkit.getPluginManager().getPlugin("floodgate") != null && cm.isBedrockLoginBypass()) {
            logger.info("检测到floodgate，基岩版兼容已装载")
        }

        PluginContext.init(this, sql, loadProtocolLib)
        registerCommands()

        Task.runAll()
    }

    private fun registerCommands() {
        registerLoginCommand()
        registerRegisterCommand()
        registerChangePasswordCommand()
        registerBindEmailCommand()
        registerResetPasswordCommand()
        registerCatSeedLoginCommand()
    }

    private fun registerLoginCommand() {
        server.getPluginCommand("login")!!.setExecutor(CommandLogin())
        server.getPluginCommand("login")!!.setTabCompleter { _, _, _, args ->
            if (args.size == 1) Collections.singletonList("密码") else ArrayList(0)
        }
    }

    private fun registerRegisterCommand() {
        server.getPluginCommand("register")!!.setExecutor(CommandRegister())
        server.getPluginCommand("register")!!.setTabCompleter { _, _, _, args ->
            if (args.size == 1) Collections.singletonList("密码 重复密码") else ArrayList(0)
        }
    }

    private fun registerChangePasswordCommand() {
        server.getPluginCommand("changepassword")!!.setExecutor(CommandChangePassword())
        server.getPluginCommand("changepassword")!!.setTabCompleter { _, _, _, args ->
            if (args.size == 1) Collections.singletonList("旧密码 新密码 重复新密码") else ArrayList(0)
        }
    }

    private fun registerBindEmailCommand() {
        val bindemail = server.getPluginCommand("bindemail")!!
        bindemail.setExecutor(CommandBindEmail())
        bindemail.setTabCompleter { _, _, _, args ->
            if (args.size == 1) {
                return@setTabCompleter listOf("set 需要绑定的邮箱", "verify 邮箱验证码")
            }
            if (args.size == 2) {
                if (args[0] == "set") {
                    return@setTabCompleter Collections.singletonList("需要绑定的邮箱")
                }
                if (args[0] == "verify") {
                    return@setTabCompleter Collections.singletonList("邮箱获取的验证码")
                }
            }
            Collections.emptyList<String>()
        }
    }

    private fun registerResetPasswordCommand() {
        val resetpassword = server.getPluginCommand("resetpassword")!!
        resetpassword.setExecutor(CommandResetPassword())
        resetpassword.setTabCompleter { _, _, _, args ->
            if (args.size == 1) {
                return@setTabCompleter listOf("forget", "re 验证码 新密码")
            }
            if (args.size == 2 && "re" == args[0]) {
                return@setTabCompleter Collections.singletonList("验证码 新密码")
            }
            if (args.size == 3 && "re" == args[0]) {
                return@setTabCompleter Collections.singletonList("新密码")
            }
            Collections.emptyList<String>()
        }
    }

    private fun registerCatSeedLoginCommand() {
        server.getPluginCommand("catseedlogin")!!.setExecutor(CommandCatSeedLogin())
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
        val cfg = configManager
        Bukkit.getOnlinePlayers().forEach { p ->
            if (LoginPlayerHelper.isLogin(p.name) && (!p.isDead || (cfg?.isDeathStateQuitRecordLocation() ?: false))) {
                Config.setOfflineLocation(p)
            }
        }

        try {
            sql?.closeConnection()
        } catch (e: Exception) {
            logger.warning("关闭数据库连接时出错")
            e.printStackTrace()
        }
        Communication.socketServerStop()
        super.onDisable()
    }

    fun runTaskAsync(runnable: Runnable?) {
        if (runnable != null) {
            CatScheduler.runTaskAsync(runnable)
        }
    }

    fun getConfigManager(): BukkitConfigManager? = configManager

    fun getPlatformAdapter(): BukkitPlatformAdapter? = platformAdapter

    fun getI18n(): I18n? = configManager?.getI18n()
}