package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache
 *
 * 全局玩家登录数据缓存。使用 [ConcurrentHashMap] 存储玩家记录，写操作通过 `synchronized` 加锁保证一致性。
 */
class Cache {
    companion object {
        private val PLAYER_HASHTABLE: MutableMap<String, LoginPlayer> = ConcurrentHashMap()

        @JvmField
        var isLoaded: Boolean = false

        @JvmStatic
        fun getAllLoginPlayer(): List<LoginPlayer> {
            synchronized(PLAYER_HASHTABLE) {
                return ArrayList(PLAYER_HASHTABLE.values)
            }
        }

        @JvmStatic
        fun getIgnoreCase(name: String): LoginPlayer? {
            return PLAYER_HASHTABLE[name.lowercase()]
        }

        @JvmStatic
        fun refreshAll() {
            isLoaded = false
            CatSeedLogin.instance?.runTaskAsync {
                try {
                    val sql = CatSeedLogin.sql
                    if (sql != null) {
                        val newCache: List<LoginPlayer> = sql.getAll()
                        synchronized(PLAYER_HASHTABLE) {
                            PLAYER_HASHTABLE.clear()
                            newCache.forEach { p ->
                                PLAYER_HASHTABLE[p.name.lowercase()] = p
                            }
                        }
                    }
                    CatSeedLogin.instance?.logger?.info("缓存加载 " + PLAYER_HASHTABLE.size + " 个数据")
                    isLoaded = true
                } catch (e: Exception) {
                    CatSeedLogin.instance?.logger?.warning("数据库错误,无法更新缓存!")
                    e.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun refresh(name: String) {
            CatSeedLogin.instance?.runTaskAsync {
                try {
                    val sql = CatSeedLogin.sql
                    if (sql != null) {
                        val newLp: LoginPlayer? = sql.get(name)
                        val key = name.lowercase()
                        synchronized(PLAYER_HASHTABLE) {
                            if (newLp != null) {
                                PLAYER_HASHTABLE[key] = newLp
                            } else {
                                PLAYER_HASHTABLE.remove(key)
                            }
                        }
                    }
                    CatSeedLogin.instance?.logger?.info("缓存加载 " + PLAYER_HASHTABLE.size + " 个数据")
                } catch (e: Exception) {
                    CatSeedLogin.instance?.logger?.warning("数据库错误,无法更新缓存!")
                    e.printStackTrace()
                }
            }
        }
    }
}