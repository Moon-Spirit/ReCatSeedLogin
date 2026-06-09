package cc.moonspirit.recatseedlogin.bukkit.object

import cc.moonspirit.recatseedlogin.common.util.DateUtil
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class EmailCode private constructor(
    val name: String,
    val email: String,
    val durability: Long
) {
    var code: String = DateUtil.generateVerificationCode()
        private set
    var createTime: Long = System.currentTimeMillis()
        private set

    enum class Type {
        Bind, ResetPassword
    }

    companion object {
        private val bindMap: MutableMap<String, EmailCode> = ConcurrentHashMap(10)
        private val resetPasswordMap: MutableMap<String, EmailCode> = ConcurrentHashMap(10)

        @JvmStatic
        fun create(name: String, email: String, durability: Long, type: Type): EmailCode? {
            return try {
                val emailCode = EmailCode(name, email, durability)
                clear()
                when (type) {
                    Type.Bind -> bindMap[name] = emailCode
                    Type.ResetPassword -> resetPasswordMap[name] = emailCode
                }
                emailCode
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun getByName(name: String, type: Type): Optional<EmailCode> {
            return try {
                clear()
                when (type) {
                    Type.Bind -> if (bindMap.containsKey(name)) Optional.ofNullable(bindMap[name]) else Optional.empty()
                    Type.ResetPassword -> if (resetPasswordMap.containsKey(name)) Optional.ofNullable(resetPasswordMap[name]) else Optional.empty()
                }
            } catch (e: Exception) {
                Optional.empty()
            }
        }

        @JvmStatic
        fun removeByName(name: String, type: Type) {
            try {
                clear()
                when (type) {
                    Type.Bind -> bindMap.remove(name)
                    Type.ResetPassword -> resetPasswordMap.remove(name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun clear() {
            val now = System.currentTimeMillis()
            bindMap.entries.removeIf { DateUtil.isExpired(it.value.createTime, it.value.durability) }
            resetPasswordMap.entries.removeIf { DateUtil.isExpired(it.value.createTime, it.value.durability) }
        }
    }

    fun isExpired(): Boolean {
        return DateUtil.isExpired(createTime, durability)
    }

    fun getRemainingTime(): Long {
        return DateUtil.getRemainingTime(createTime, durability)
    }
}