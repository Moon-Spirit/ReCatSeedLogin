package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.bukkit.config.BukkitConfigManager
import cc.moonspirit.recatseedlogin.common.config.ConfigConstants
import cc.moonspirit.recatseedlogin.common.i18n.MessageKey
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.io.BufferedInputStream
import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * 配置容器 - 持有插件运行期的全部配置信息。
 *
 * 该对象是单例容器（非平台入口），使用 Kotlin `object` 表达。
 * 每个嵌套对象对应 Java 源码中的一组静态配置。
 */
object Config {
    private val plugin: CatSeedLogin = CatSeedLogin.instance ?: error("CatSeedLogin not initialized")

    object MySQL {
        var Enable: Boolean = false
        var Host: String = ""
        var Port: String = ""
        var Database: String = ""
        var User: String = ""
        var Password: String = ""

        fun load() {
            val cm = plugin.configManager ?: error("configManager not initialized")
            Enable = cm.isMySQL()
            Host = cm.getDatabaseHost()
            Port = cm.getDatabasePort().toString()
            Database = cm.getDatabaseName()
            User = cm.getDatabaseUser()
            Password = cm.getDatabasePassword()
        }
    }

    object BungeeCord {
        var Enable: Boolean = false
        var Host: String = ""
        var Port: String = ""
        var AuthKey: String = ""

        fun load() {
            val cm = plugin.configManager ?: error("configManager not initialized")
            Enable = cm.isEnable()
            Host = cm.getProxyHost()
            Port = cm.getProxyPort().toString()
            AuthKey = cm.getAuthKey()
        }
    }

    object Settings {
        var IpRegisterCountLimit: Int = 0
        var IpCountLimit: Int = 0
        var SpawnLocation: Location? = null
        var LimitChineseID: Boolean = false
        var BedrockLoginBypass: Boolean = false
        var LoginwiththesameIP: Boolean = false
        var EmptyBackpack: Boolean = false
        var IPTimeout: Int = 0
        var MaxLengthID: Int = 0
        var MinLengthID: Int = 0
        var BeforeLoginNoDamage: Boolean = false
        var ReenterInterval: Long = 0L
        var AfterLoginBack: Boolean = false
        var CanTpSpawnLocation: Boolean = false

        /**
         * 保留可变列表及其线程安全语义（与 Java 中 `new ArrayList<>()` 行为一致）。
         */
        var CommandWhiteList: MutableList<Pattern> = mutableListOf()

        var AutoKick: Int = 0
        var NamePattern: String = ""
        var DeathStateQuitRecordLocation: Boolean = false
        var FloodgatePrefixProtect: Boolean = false

        fun load() {
            val cm = plugin.configManager ?: error("configManager not initialized")
            IpRegisterCountLimit = cm.getIpRegisterCountLimit()
            IpCountLimit = cm.getIpCountLimit()
            LimitChineseID = cm.isLimitChineseID()
            MinLengthID = cm.getMinLengthID()
            BedrockLoginBypass = cm.isBedrockLoginBypass()
            LoginwiththesameIP = cm.isLoginWithSameIP()
            EmptyBackpack = cm.isEmptyBackpack()
            MaxLengthID = cm.getMaxLengthID()
            BeforeLoginNoDamage = cm.isBeforeLoginNoDamage()
            ReenterInterval = cm.getReenterInterval()
            AfterLoginBack = cm.isAfterLoginBack()
            CanTpSpawnLocation = cm.isCanTpSpawnLocation()
            NamePattern = cm.getNamePattern()
            CommandWhiteList = cm.getCommandWhiteList().toMutableList()
            AutoKick = cm.getAutoKick()
            IPTimeout = cm.getIPTimeout()
            SpawnLocation = cm.getBukkitSpawnLocation()
            DeathStateQuitRecordLocation = cm.isDeathStateQuitRecordLocation()
            FloodgatePrefixProtect = cm.isFloodgatePrefixProtect()
        }

        fun save() {
            val cm = plugin.configManager ?: error("configManager not initialized")
            cm.set(ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT, IpRegisterCountLimit)
            cm.set(ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT, IpCountLimit)
            cm.set(ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID, LimitChineseID)
            cm.set(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, BedrockLoginBypass)
            cm.set(ConfigConstants.Path.SAME_IP_ENABLED, LoginwiththesameIP)
            cm.set(ConfigConstants.Path.EMPTY_BACKPACK, EmptyBackpack)
            cm.set(ConfigConstants.Path.SAME_IP_TIMEOUT, IPTimeout)
            cm.set(ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, MinLengthID)
            cm.set(ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, MaxLengthID)
            cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, BeforeLoginNoDamage)
            cm.set(ConfigConstants.Path.SETTINGS_REENTER_INTERVAL, ReenterInterval)
            cm.set(ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK, AfterLoginBack)
            cm.set(ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION, CanTpSpawnLocation)
            cm.set(ConfigConstants.Path.SETTINGS_AUTO_KICK, AutoKick)
            cm.set(ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD, DeathStateQuitRecordLocation)
            cm.set(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, FloodgatePrefixProtect)
            cm.set(ConfigConstants.Path.SETTINGS_NAME_PATTERN, NamePattern)

            if (CommandWhiteList.isNotEmpty()) {
                cm.getMainConfig().set(
                    ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST,
                    CommandWhiteList.stream().map { obj: Pattern -> obj.toString() }
                        .collect(Collectors.toList())
                )
            }

            SpawnLocation?.let { cm.setSpawnLocation(it) }
        }
    }

    object Language {
        var LOGIN_REQUEST: String = ""
        var REGISTER_REQUEST: String = ""
        var LOGIN_NOREGISTER: String = ""
        var LOGIN_REPEAT: String = ""
        var LOGIN_SUCCESS: String = ""
        var LOGIN_FAIL: String = ""
        var LOGIN_FAIL_IF_FORGET: String = ""
        var REGISTER_SUCCESS: String = ""
        var REGISTER_BEFORE_LOGIN_ALREADY: String = ""
        var REGISTER_AFTER_LOGIN_ALREADY: String = ""
        var REGISTER_PASSWORD_CONFIRM_FAIL: String = ""
        var COMMON_PASSWORD_SO_SIMPLE: String = ""
        var RESETPASSWORD_NOREGISTER: String = ""
        var RESETPASSWORD_EMAIL_DISABLE: String = ""
        var RESETPASSWORD_EMAIL_NO_SET: String = ""
        var RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE: String = ""
        var RESETPASSWORD_EMAIL_SENDING_MESSAGE: String = ""
        var RESETPASSWORD_EMAIL_SENT_MESSAGE: String = ""
        var RESETPASSWORD_EMAIL_WARN: String = ""
        var RESETPASSWORD_SUCCESS: String = ""
        var RESETPASSWORD_EMAILCODE_INCORRECT: String = ""
        var RESETPASSWORD_FAIL: String = ""
        var CHANGEPASSWORD_NOREGISTER: String = ""
        var CHANGEPASSWORD_NOLOGIN: String = ""
        var CHANGEPASSWORD_OLDPASSWORD_INCORRECT: String = ""
        var CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL: String = ""
        var CHANGEPASSWORD_SUCCESS: String = ""
        var AUTO_KICK: String = ""
        var REGISTER_MORE: String = ""
        var BEDROCK_LOGIN_BYPASS: String = ""
        var LOGIN_WITH_THE_SAME_IP: String = ""

        fun load() {
            LOGIN_REQUEST = MessageKey.LOGIN_REQUEST.get() ?: error("Missing message: LOGIN_REQUEST")
            REGISTER_REQUEST = MessageKey.REGISTER_REQUEST.get() ?: error("Missing message: REGISTER_REQUEST")
            LOGIN_NOREGISTER = MessageKey.LOGIN_NOREGISTER.get() ?: error("Missing message: LOGIN_NOREGISTER")
            LOGIN_REPEAT = MessageKey.LOGIN_REPEAT.get() ?: error("Missing message: LOGIN_REPEAT")
            LOGIN_SUCCESS = MessageKey.LOGIN_SUCCESS.get() ?: error("Missing message: LOGIN_SUCCESS")
            LOGIN_FAIL = MessageKey.LOGIN_FAIL.get() ?: error("Missing message: LOGIN_FAIL")
            LOGIN_FAIL_IF_FORGET = MessageKey.LOGIN_FAIL_IF_FORGET.get() ?: error("Missing message: LOGIN_FAIL_IF_FORGET")
            REGISTER_SUCCESS = MessageKey.REGISTER_SUCCESS.get() ?: error("Missing message: REGISTER_SUCCESS")
            REGISTER_BEFORE_LOGIN_ALREADY = MessageKey.REGISTER_BEFORE_LOGIN_ALREADY.get() ?: error("Missing message: REGISTER_BEFORE_LOGIN_ALREADY")
            REGISTER_AFTER_LOGIN_ALREADY = MessageKey.REGISTER_AFTER_LOGIN_ALREADY.get() ?: error("Missing message: REGISTER_AFTER_LOGIN_ALREADY")
            REGISTER_PASSWORD_CONFIRM_FAIL = MessageKey.REGISTER_PASSWORD_CONFIRM_FAIL.get() ?: error("Missing message: REGISTER_PASSWORD_CONFIRM_FAIL")
            COMMON_PASSWORD_SO_SIMPLE = MessageKey.COMMON_PASSWORD_SO_SIMPLE.get() ?: error("Missing message: COMMON_PASSWORD_SO_SIMPLE")
            RESETPASSWORD_NOREGISTER = MessageKey.RESETPASSWORD_NOREGISTER.get() ?: error("Missing message: RESETPASSWORD_NOREGISTER")
            RESETPASSWORD_EMAIL_DISABLE = MessageKey.RESETPASSWORD_EMAIL_DISABLE.get() ?: error("Missing message: RESETPASSWORD_EMAIL_DISABLE")
            RESETPASSWORD_EMAIL_NO_SET = MessageKey.RESETPASSWORD_EMAIL_NO_SET.get() ?: error("Missing message: RESETPASSWORD_EMAIL_NO_SET")
            RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE =
                MessageKey.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE.get() ?: error("Missing message: RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE")
            RESETPASSWORD_EMAIL_SENDING_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_SENDING_MESSAGE.get() ?: error("Missing message: RESETPASSWORD_EMAIL_SENDING_MESSAGE")
            RESETPASSWORD_EMAIL_SENT_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_SENT_MESSAGE.get() ?: error("Missing message: RESETPASSWORD_EMAIL_SENT_MESSAGE")
            RESETPASSWORD_EMAIL_WARN = MessageKey.RESETPASSWORD_EMAIL_WARN.get() ?: error("Missing message: RESETPASSWORD_EMAIL_WARN")
            RESETPASSWORD_SUCCESS = MessageKey.RESETPASSWORD_SUCCESS.get() ?: error("Missing message: RESETPASSWORD_SUCCESS")
            RESETPASSWORD_EMAILCODE_INCORRECT = MessageKey.RESETPASSWORD_EMAILCODE_INCORRECT.get() ?: error("Missing message: RESETPASSWORD_EMAILCODE_INCORRECT")
            RESETPASSWORD_FAIL = MessageKey.RESETPASSWORD_FAIL.get() ?: error("Missing message: RESETPASSWORD_FAIL")
            CHANGEPASSWORD_NOREGISTER = MessageKey.CHANGEPASSWORD_NOREGISTER.get() ?: error("Missing message: CHANGEPASSWORD_NOREGISTER")
            CHANGEPASSWORD_NOLOGIN = MessageKey.CHANGEPASSWORD_NOLOGIN.get() ?: error("Missing message: CHANGEPASSWORD_NOLOGIN")
            CHANGEPASSWORD_OLDPASSWORD_INCORRECT = MessageKey.CHANGEPASSWORD_OLDPASSWORD_INCORRECT.get() ?: error("Missing message: CHANGEPASSWORD_OLDPASSWORD_INCORRECT")
            CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL = MessageKey.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL.get() ?: error("Missing message: CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL")
            CHANGEPASSWORD_SUCCESS = MessageKey.CHANGEPASSWORD_SUCCESS.get() ?: error("Missing message: CHANGEPASSWORD_SUCCESS")
            AUTO_KICK = MessageKey.AUTO_KICK.get() ?: error("Missing message: AUTO_KICK")
            REGISTER_MORE = MessageKey.REGISTER_MORE.get() ?: error("Missing message: REGISTER_MORE")
            BEDROCK_LOGIN_BYPASS = MessageKey.BEDROCK_LOGIN_BYPASS.get() ?: error("Missing message: BEDROCK_LOGIN_BYPASS")
            LOGIN_WITH_THE_SAME_IP = MessageKey.LOGIN_WITH_THE_SAME_IP.get() ?: error("Missing message: LOGIN_WITH_THE_SAME_IP")
        }
    }

    object EmailVerify {
        var Enable: Boolean = false
        var EmailAccount: String = ""
        var EmailPassword: String = ""
        var EmailSmtpHost: String = ""
        var EmailSmtpPort: String = ""
        var SSLAuthVerify: Boolean = false
        var FromPersonal: String = ""

        fun load() {
            val cm = plugin.configManager ?: error("configManager not initialized")
            Enable = cm.isEmailEnable()
            EmailAccount = cm.getEmailAccount()
            EmailPassword = cm.getEmailPassword()
            EmailSmtpHost = cm.getEmailSmtpHost()
            EmailSmtpPort = cm.getEmailSmtpPort()
            SSLAuthVerify = cm.isSSLAuthVerify()
            FromPersonal = cm.getFromPersonal()
        }
    }

    fun load() {
        val cm = plugin.configManager ?: error("configManager not initialized")
        cm.createDefaultConfig("config.yml")
        MySQL.load()
        Settings.load()
        EmailVerify.load()
        Language.load()
        BungeeCord.load()
    }

    fun save() {
        Settings.save()
    }

    fun reload() {
        plugin.configManager?.reload()
        load()
    }

    fun getOfflineLocation(player: Player): Location? {
        return try {
            val locStr = CatSeedLogin.sql?.getLocation(player.name)
            if (!locStr.isNullOrEmpty()) {
                str2Location(locStr)
            } else {
                null
            }
        } catch (e: Exception) {
            plugin.logger.warning("获取玩家离线位置失败: " + player.name)
            e.printStackTrace()
            null
        }
    }

    fun setOfflineLocation(player: Player) {
        val locStr = loc2String(player.location)
        plugin.runTaskAsync {
            try {
                CatSeedLogin.sql?.updateLocation(player.name, locStr)
            } catch (e: Exception) {
                plugin.logger.warning("保存玩家离线位置失败: " + player.name)
                e.printStackTrace()
            }
        }
    }

    private fun str2Location(str: String): Location {
        var loc: Location
        try {
            val locStrs = str.split(":")
            val world = Bukkit.getWorld(locStrs[0]) ?: Bukkit.getWorlds()[0]
            val x = locStrs[1].toDouble()
            val y = locStrs[2].toDouble()
            val z = locStrs[3].toDouble()
            val yaw = locStrs[4].toFloat()
            val pitch = locStrs[5].toFloat()
            loc = Location(world, x, y, z, yaw, pitch)
        } catch (ignored: Exception) {
            loc = defaultWorld.spawnLocation
        }
        return loc
    }

    private fun loc2String(loc: Location): String {
        return try {
            String.format(
                "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
                loc.world?.name ?: "unknown",
                loc.x,
                loc.y,
                loc.z,
                loc.yaw,
                loc.pitch
            )
        } catch (e: Exception) {
            e.printStackTrace()
            val defaultLoc = defaultWorld.spawnLocation
            String.format(
                "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
                defaultLoc.world?.name ?: "unknown",
                defaultLoc.x,
                defaultLoc.y,
                defaultLoc.z,
                defaultLoc.yaw,
                defaultLoc.pitch
            )
        }
    }

    private val defaultWorld: World
        get() {
            val serverPropertiesFile = File("server.properties")
            if (!serverPropertiesFile.exists()) {
                return Bukkit.getWorlds()[0]
            }
            try {
                BufferedInputStream(Files.newInputStream(serverPropertiesFile.toPath())).use { stream ->
                    val properties = Properties()
                    properties.load(stream)
                    val worldName = properties.getProperty("level-name")
                    val world = Bukkit.getWorld(worldName)
                    if (world != null) {
                        return world
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Bukkit.getWorlds()[0]
        }
}