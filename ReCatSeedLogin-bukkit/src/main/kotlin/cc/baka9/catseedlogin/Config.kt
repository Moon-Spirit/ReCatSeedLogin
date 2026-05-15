package cc.baka9.catseedlogin

import org.yaml.snakeyaml.Yaml

object Config {
    
    object Language {
        var NEED_LOGIN = "&c请先登录! 使用 &e/login <密码>"
        var ALREADY_LOGIN = "&c你已经登录了!"
        var LOGIN_SUCCESS = "&a登录成功!"
        var LOGIN_FAILED = "&c密码错误!"
        var NEED_REGISTER = "&c请先注册! 使用 &e/register <密码> <重复密码>"
        var ALREADY_REGISTER = "&c你已经注册过了!"
        var REGISTER_SUCCESS = "&a注册成功!"
        var PASSWORD_MISMATCH = "&c两次密码输入不一致!"
        var BEDROCK_LOGIN_BYPASS = "&a检测到基岩版玩家,登录已跳过"
        var LOGIN_WITH_THE_SAME_IP = "&a检测到相同IP登录,登录已跳过"
    }
    
    object Settings {
        var ipCountLimit = 2
        var ipRegCountLimit = 2
        var minLengthID = 2
        var maxLengthID = 15
        var reenterInterval = 60L
        var autoKick = 120
        var spawnLocation: org.bukkit.Location? = null
        var limitChineseID = true
        var bedrockLoginBypass = true
        var loginwiththesameIP = false
        var beforeLoginNoDamage = true
        var afterLoginBack = true
        var canTpSpawnLocation = true
        var deathStateQuitRecordLocation = true
        var floodgatePrefixProtect = true
        var commandWhiteList: List<java.util.regex.Pattern> = listOf()
        var namePattern: java.util.regex.Pattern = "^[a-zA-Z0-9_]+$".toRegex()
    }
    
    private var mainConfig: YamlConfiguration? = null
    
    fun load() {
        mainConfig = ReCatSeedLogin.instance?.configManagerInstance?.mainConfig
        loadLanguage()
        loadSettings()
    }
    
    private fun loadLanguage() {
        val lang = mainConfig?.getString("language", "zh-CN") ?: "zh-CN"
        // Load language file based on lang
    }
    
    private fun loadSettings() {
        val settings = mainConfig?.getSection("settings")
        Settings.ipCountLimit = mainConfig?.getInt("settings.ipCountLimit", 2) ?: 2
        Settings.ipRegCountLimit = mainConfig?.getInt("settings.ipRegCountLimit", 2) ?: 2
        Settings.minLengthID = mainConfig?.getInt("settings.minLengthID", 2) ?: 2
        Settings.maxLengthID = mainConfig?.getInt("settings.maxLengthID", 15) ?: 15
        Settings.reenterInterval = mainConfig?.getLong("settings.reenterInterval", 60L) ?: 60L
        Settings.autoKick = mainConfig?.getInt("settings.autoKick", 120) ?: 120
        Settings.limitChineseID = mainConfig?.getBoolean("settings.limitChineseID", true) ?: true
        Settings.bedrockLoginBypass = mainConfig?.getBoolean("settings.bedrockLoginBypass", true) ?: true
        Settings.loginwiththesameIP = mainConfig?.getBoolean("settings.loginwiththesameIP", false) ?: false
        Settings.beforeLoginNoDamage = mainConfig?.getBoolean("settings.beforeLoginNoDamage", true) ?: true
        Settings.afterLoginBack = mainConfig?.getBoolean("settings.afterLoginBack", true) ?: true
        Settings.canTpSpawnLocation = mainConfig?.getBoolean("settings.canTpSpawnLocation", true) ?: true
        Settings.deathStateQuitRecordLocation = mainConfig?.getBoolean("settings.deathStateQuitRecordLocation", true) ?: true
        
        val cmdWhitelist = mainConfig?.getStringList("settings.commandWhiteList") ?: listOf()
        Settings.commandWhiteList = cmdWhitelist.map { it.toRegex() }
    }
    
    fun setOfflineLocation(player: org.bukkit.entity.Player) {
        val location = player.location
        val locStr = "${location.world.name}:${location.x}:${location.y}:${location.z}:${location.yaw}:${location.pitch}"
        mainConfig?.set("offlineLocation.${player.name}", locStr)
    }
    
    fun getOfflineLocation(player: org.bukkit.entity.Player): java.util.Optional<org.bukkit.Location>? {
        val locStr = mainConfig?.getString("offlineLocation.${player.name}") ?: return null
        return try {
            val parts = locStr.split(":")
            val world = org.bukkit.Bukkit.getWorld(parts[0]) ?: return null
            java.util.Optional.of(org.bukkit.Location(world, 
                parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(),
                parts[4].toFloat(), parts[5].toFloat()))
        } catch (e: Exception) {
            null
        }
    }
}