package cc.baka9.catseedlogin

import cc.baka9.catseedlogin.common.api.PlatformAdapter
import cc.baka9.catseedlogin.common.i18n.I18n

class BukkitPlatformAdapter(
    private val plugin: ReCatSeedLogin,
    override val i18n: I18n
) : PlatformAdapter {
    
    override fun getLogger(): org.bukkit.Logger {
        return plugin.logger
    }
    
    override fun getDataFolder(): File {
        return plugin.dataFolder
    }
}