package cc.moonspirit.recatseedlogin.bukkit.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.Optional

class CatSeedPlayerLoginEvent(val player: Player, private val email: String?, val result: Result) : Event() {

    override fun getHandlers(): HandlerList = handlers

    fun getEmail(): Optional<String> {
        return Optional.ofNullable(email)
    }

    enum class Result {
        SUCCESS, FAIL;

        constructor() {}
    }

    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }
}