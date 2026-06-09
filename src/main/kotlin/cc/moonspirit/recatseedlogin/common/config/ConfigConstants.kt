package cc.moonspirit.recatseedlogin.common.config

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object ConfigConstants {

    const val DEFAULT_LANGUAGE: String = "zh_CN"
    const val DEFAULT_PROXY_HOST: String = "127.0.0.1"
    const val DEFAULT_PROXY_PORT: Int = 2333
    const val DEFAULT_MYSQL_PORT: Int = 3306
    const val DEFAULT_DATABASE_NAME: String = "catseedlogin"
    const val DEFAULT_SPAWN_LOCATION: String = "world:0:64:0:0:0"
    const val DEFAULT_NAME_PATTERN: String = "^\\w+$"
    const val DEFAULT_IP_REGISTER_LIMIT: Int = 2
    const val DEFAULT_IP_LOGIN_LIMIT: Int = 2
    const val DEFAULT_MIN_NAME_LENGTH: Int = 2
    const val DEFAULT_MAX_NAME_LENGTH: Int = 15
    const val DEFAULT_AUTO_KICK_SECONDS: Int = 120
    const val DEFAULT_REENTER_INTERVAL_TICKS: Int = 60
    const val DEFAULT_IP_TIMEOUT_MINUTES: Int = 5
    const val DEFAULT_SMTP_HOST: String = "smtp.example.com"
    const val DEFAULT_SMTP_PORT: String = "465"
    const val DEFAULT_FROM_NAME: String = "Server"

    val DEFAULT_COMMAND_WHITELIST: List<String> = listOf(
        "/(?i)l(ogin)?(\\z| .*)",
        "/(?i)reg(ister)?(\\z| .*)",
        "/(?i)resetpassword?(\\z| .*)",
        "/(?i)repw?(\\z| .*)"
    )

    fun compilePatternOrDefault(pattern: String?, defaultPattern: String): Pattern {
        if (pattern.isNullOrEmpty()) {
            return Pattern.compile(defaultPattern)
        }
        return try {
            Pattern.compile(pattern)
        } catch (e: PatternSyntaxException) {
            Pattern.compile(defaultPattern)
        }
    }

    fun compilePatterns(patterns: List<String>?): List<Pattern> {
        val result = mutableListOf<Pattern>()
        if (patterns.isNullOrEmpty()) {
            return result
        }
        for (pattern in patterns) {
            try {
                result.add(Pattern.compile(pattern))
            } catch (e: PatternSyntaxException) {
                // Skip invalid patterns
            }
        }
        return result
    }

    object Path {
        const val SETTINGS_IP_REGISTER_LIMIT = "settings.ip-register-count-limit"
        const val SETTINGS_IP_COUNT_LIMIT = "settings.ip-count-limit"
        const val SETTINGS_LIMIT_CHINESE_ID = "settings.limit-chinese-id"
        const val SETTINGS_MIN_LENGTH_ID = "settings.min-length-id"
        const val SETTINGS_MAX_LENGTH_ID = "settings.max-length-id"
        const val SETTINGS_BEFORE_LOGIN_NO_DAMAGE = "settings.before-login-no-damage"
        const val SETTINGS_REENTER_INTERVAL = "settings.reenter-interval"
        const val SETTINGS_AFTER_LOGIN_BACK = "settings.after-login-back"
        const val SETTINGS_CAN_TP_SPAWN_LOCATION = "settings.can-tp-spawn-location"
        const val SETTINGS_AUTO_KICK = "settings.auto-kick"
        const val SETTINGS_NAME_PATTERN = "settings.name-pattern"
        const val SETTINGS_DEATH_STATE_QUIT_RECORD = "settings.death-state-quit-record-location"
        const val SETTINGS_COMMAND_WHITELIST = "settings.command-white-list"

        const val BEDROCK_LOGIN_BYPASS = "bedrock.login-bypass"
        const val BEDROCK_FLOODGATE_PREFIX = "bedrock.floodgate-prefix-protect"

        const val SAME_IP_ENABLED = "same-ip-login.enabled"
        const val SAME_IP_TIMEOUT = "same-ip-login.timeout"

        const val EMPTY_BACKPACK = "empty-backpack"

        const val SPAWN_LOCATION = "spawn.location"

        const val DATABASE_MYSQL = "database.mysql"
        const val DATABASE_HOST = "database.host"
        const val DATABASE_PORT = "database.port"
        const val DATABASE_NAME = "database.database"
        const val DATABASE_USER = "database.user"
        const val DATABASE_PASSWORD = "database.password"

        const val EMAIL_ENABLED = "email.enabled"
        const val EMAIL_ACCOUNT = "email.account"
        const val EMAIL_PASSWORD = "email.password"
        const val EMAIL_SMTP_HOST = "email.smtp-host"
        const val EMAIL_SMTP_PORT = "email.smtp-port"
        const val EMAIL_SSL_AUTH = "email.ssl-auth"
        const val EMAIL_FROM_NAME = "email.from-name"

        const val PROXY_ENABLED = "proxy.enabled"
        const val PROXY_HOST = "proxy.host"
        const val PROXY_PORT = "proxy.port"
        const val PROXY_AUTH_KEY = "proxy.auth-key"
        const val PROXY_LOGIN_SERVER_NAME = "proxy.login-server-name"

        const val LANGUAGE = "language"
    }

    object Comment {
        const val DATABASE_HOST = "数据库主机地址 (MySQL服务器IP)"
        const val DATABASE_PORT = "数据库端口 (MySQL默认3306)"
        const val DATABASE_NAME = "数据库名称"
        const val DATABASE_USER = "数据库用户名"
        const val DATABASE_PASSWORD = "数据库密码"

        const val PROXY_HOST = "代理服务器通信地址 (Bukkit用于监听BungeeCord/Velocity连接)"
        const val PROXY_PORT = "代理服务器通信端口 (Bukkit监听端口)"
        const val PROXY_AUTH_KEY = "代理通信认证密钥 (确保Bukkit与代理通信安全)"
        const val PROXY_LOGIN_SERVER_NAME = "登录服服务器名称 (在代理端配置的服务器名)"
    }
}