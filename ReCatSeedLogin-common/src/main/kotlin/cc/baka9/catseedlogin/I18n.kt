package cc.baka9.catseedlogin

class I18n(language: String) {
    
    private val messages: MutableMap<String, String> = mutableMapOf()
    
    companion object {
        lateinit var instance: I18n
            private set
        fun setInstance(i18n: I18n) { instance = i18n }
    }
    
    init {
        loadMessages(language)
    }
    
    private fun loadMessages(language: String) {
        val defaultMessages = mapOf(
            "need-login" to "&c请先登录! 使用 &e/login <密码>",
            "already-login" to "&c你已经登录了!",
            "login-success" to "&a登录成功!",
            "login-failed" to "&c密码错误!",
            "need-register" to "&c请先注册! 使用 &e/register <密码> <重复密码>",
            "already-register" to "&c你已经注册过了!",
            "register-success" to "&a注册成功!",
            "password-mismatch" to "&c两次密码输入不一致!",
            "invalid-command" to "&c无效的指令!",
            "no-permission" to "&c你没有权限使用这个指令!",
            "player-not-found" to "&c玩家不存在!",
            "email-sent" to "&a验证码已发送到你的邮箱!",
            "invalid-code" to "&c无效的验证码!",
            "password-reset-success" to "&a密码重置成功!"
        )
        messages.putAll(defaultMessages)
    }
    
    fun get(key: String): String {
        return messages[key] ?: key
    }
    
    fun set(key: String, value: String) {
        messages[key] = value
    }
}