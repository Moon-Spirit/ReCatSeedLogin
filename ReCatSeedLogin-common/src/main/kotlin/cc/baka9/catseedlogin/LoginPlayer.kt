package cc.baka9.catseedlogin

import java.util.HashMap

class LoginPlayer(
    var name: String,
    var password: String
) {
    var email: String? = null
    var ips: String? = null
    var lastAction: Long = 0
    var location: String? = null

    constructor(name: String, password: String, email: String?, ips: String?, lastAction: Long, location: String?) : this(name, password) {
        this.email = email
        this.ips = ips
        this.lastAction = lastAction
        this.location = location
    }

    fun getIpsList(): List<String> {
        return if (ips != null) {
            ips!!.split(";").toMutableList()
        } else {
            mutableListOf()
        }
    }

    fun crypt() {
        password = Crypt.encrypt(name, password) ?: password
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is LoginPlayer && name == other.name)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "LoginPlayer(name='$name', password='$password', email=$email, ips=$ips, lastAction=$lastAction, location=$location)"
    }
}