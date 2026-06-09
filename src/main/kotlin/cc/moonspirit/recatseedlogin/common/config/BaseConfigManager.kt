package cc.moonspirit.recatseedlogin.common.config

import cc.moonspirit.recatseedlogin.common.api.BungeeCordConfig
import cc.moonspirit.recatseedlogin.common.api.CoreConfig
import cc.moonspirit.recatseedlogin.common.api.DatabaseConfig
import cc.moonspirit.recatseedlogin.common.api.EmailConfig
import cc.moonspirit.recatseedlogin.common.api.SpawnLocation
import cc.moonspirit.recatseedlogin.common.i18n.I18n
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern

abstract class BaseConfigManager : CoreConfig, DatabaseConfig, BungeeCordConfig, EmailConfig {

    protected var dataFolder: File? = null
    protected var i18n: I18n? = null
    protected var mainConfig: YamlConfiguration? = null

    private val cfg: YamlConfiguration
        get() = mainConfig ?: error("mainConfig not initialized; call initConfig() first")

    private val i18nInstance: I18n
        get() = i18n ?: error("i18n not initialized; call initConfig() first")

    protected fun initConfig(dataFolder: File, configFileName: String) {
        this.dataFolder = dataFolder
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        createDefaultConfig(configFileName)
        mainConfig = getConfig(configFileName)
        i18n = I18n(dataFolder, object : I18n.ResourceProvider {
            override fun getResource(name: String): InputStream? = this@BaseConfigManager.getResource(name)
        })
        val language = cfg.getString(
            ConfigConstants.Path.LANGUAGE,
            ConfigConstants.DEFAULT_LANGUAGE
        )
        i18nInstance.setLocale(language.replace("_", "-"))
    }

    abstract fun getResource(name: String): InputStream?

    abstract fun getConfig(name: String): YamlConfiguration

    abstract fun createDefaultConfig(name: String?)

    abstract fun saveConfig(name: String?)

    open fun reload() {
        reloadAll()
        mainConfig = getConfig("config.yml")
        val language = cfg.getString(
            ConfigConstants.Path.LANGUAGE,
            ConfigConstants.DEFAULT_LANGUAGE
        )
        i18nInstance.setLocale(language.replace("_", "-"))
        i18nInstance.reload()
    }

    open fun reloadAll() {
        mainConfig = getConfig("config.yml")
    }

    fun getI18n(): I18n? = i18n

    fun getMainConfig(): YamlConfiguration = cfg

    override fun getIpRegisterCountLimit(): Int =
        cfg.getInt(
            ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT,
            ConfigConstants.DEFAULT_IP_REGISTER_LIMIT
        )

    override fun getIpCountLimit(): Int =
        cfg.getInt(
            ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT,
            ConfigConstants.DEFAULT_IP_LOGIN_LIMIT
        )

    override fun isLimitChineseID(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID, true)

    override fun isBedrockLoginBypass(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, true)

    override fun isLoginWithSameIP(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.SAME_IP_ENABLED, false)

    override fun isEmptyBackpack(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.EMPTY_BACKPACK, true)

    override fun getIPTimeout(): Int =
        cfg.getInt(
            ConfigConstants.Path.SAME_IP_TIMEOUT,
            ConfigConstants.DEFAULT_IP_TIMEOUT_MINUTES
        )

    override fun getMaxLengthID(): Int =
        cfg.getInt(
            ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID,
            ConfigConstants.DEFAULT_MAX_NAME_LENGTH
        )

    override fun getMinLengthID(): Int =
        cfg.getInt(
            ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID,
            ConfigConstants.DEFAULT_MIN_NAME_LENGTH
        )

    override fun isBeforeLoginNoDamage(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, true)

    override fun getReenterInterval(): Long =
        cfg.getLong(
            ConfigConstants.Path.SETTINGS_REENTER_INTERVAL,
            ConfigConstants.DEFAULT_REENTER_INTERVAL_TICKS.toLong()
        )

    override fun isAfterLoginBack(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK, true)

    override fun isCanTpSpawnLocation(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION, true)

    override fun getAutoKick(): Int =
        cfg.getInt(
            ConfigConstants.Path.SETTINGS_AUTO_KICK,
            ConfigConstants.DEFAULT_AUTO_KICK_SECONDS
        )

    override fun getNamePattern(): String =
        cfg.getString(
            ConfigConstants.Path.SETTINGS_NAME_PATTERN,
            ConfigConstants.DEFAULT_NAME_PATTERN
        )

    override fun isDeathStateQuitRecordLocation(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD, true)

    override fun isFloodgatePrefixProtect(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, true)

    override fun getCommandWhiteList(): List<Pattern> {
        val patterns = cfg.getStringList(ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST)
        return ConfigConstants.compilePatterns(patterns)
    }

    override fun isMySQL(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.DATABASE_MYSQL, false)

    override fun getDatabaseHost(): String =
        cfg.getString(
            ConfigConstants.Path.DATABASE_HOST,
            ConfigConstants.DEFAULT_PROXY_HOST
        )

    override fun getDatabasePort(): Int =
        cfg.getInt(ConfigConstants.Path.DATABASE_PORT, ConfigConstants.DEFAULT_MYSQL_PORT)

    override fun getDatabaseName(): String =
        cfg.getString(
            ConfigConstants.Path.DATABASE_NAME,
            ConfigConstants.DEFAULT_DATABASE_NAME
        )

    override fun getDatabaseUser(): String =
        cfg.getString(ConfigConstants.Path.DATABASE_USER, "root")

    override fun getDatabasePassword(): String =
        cfg.getString(ConfigConstants.Path.DATABASE_PASSWORD, "password")

    override fun isEnable(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.PROXY_ENABLED, false)

    override fun getProxyHost(): String =
        cfg.getString(
            ConfigConstants.Path.PROXY_HOST,
            ConfigConstants.DEFAULT_PROXY_HOST
        )

    override fun getProxyPort(): Int =
        cfg.getInt(ConfigConstants.Path.PROXY_PORT, ConfigConstants.DEFAULT_PROXY_PORT)

    override fun getAuthKey(): String =
        cfg.getString(ConfigConstants.Path.PROXY_AUTH_KEY, "")

    override fun getLoginServerName(): String =
        cfg.getString(ConfigConstants.Path.PROXY_LOGIN_SERVER_NAME, "lobby")

    override fun isEmailEnable(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.EMAIL_ENABLED, false)

    override fun getEmailAccount(): String =
        cfg.getString(ConfigConstants.Path.EMAIL_ACCOUNT, "")

    override fun getEmailPassword(): String =
        cfg.getString(ConfigConstants.Path.EMAIL_PASSWORD, "")

    override fun getEmailSmtpHost(): String =
        cfg.getString(
            ConfigConstants.Path.EMAIL_SMTP_HOST,
            ConfigConstants.DEFAULT_SMTP_HOST
        )

    override fun getEmailSmtpPort(): String =
        cfg.getString(
            ConfigConstants.Path.EMAIL_SMTP_PORT,
            ConfigConstants.DEFAULT_SMTP_PORT
        )

    override fun isSSLAuthVerify(): Boolean =
        cfg.getBoolean(ConfigConstants.Path.EMAIL_SSL_AUTH, true)

    override fun getFromPersonal(): String =
        cfg.getString(
            ConfigConstants.Path.EMAIL_FROM_NAME,
            ConfigConstants.DEFAULT_FROM_NAME
        )

    fun set(path: String, value: Any?) {
        cfg.set(path, value)
        saveConfig("config.yml")
    }

    override fun getSpawnLocation(): SpawnLocation {
        val locStr = cfg.getString(
            ConfigConstants.Path.SPAWN_LOCATION,
            ConfigConstants.DEFAULT_SPAWN_LOCATION
        )
        return parseSpawnLocation(locStr)
    }

    private fun parseSpawnLocation(str: String): SpawnLocation {
        val data = ConfigHelper.parseLocationString(
            str,
            ConfigHelper.LocationData("world", 0.0, 64.0, 0.0, 0.0f, 0.0f)
        )
        return object : SpawnLocation {
            override fun getWorld(): String = data.world
            override fun getX(): Double = data.x
            override fun getY(): Double = data.y
            override fun getZ(): Double = data.z
            override fun getYaw(): Float = data.yaw
            override fun getPitch(): Float = data.pitch
        }
    }
}