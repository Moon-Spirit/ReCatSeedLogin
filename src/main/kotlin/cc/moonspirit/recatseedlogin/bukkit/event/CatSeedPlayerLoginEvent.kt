/*
 * Original work: CatSeedLogin
 * Copyright (c) 2021 CatSeed
 *
 * Licensed under the MIT License - see the LICENSE file for details.
 * (Original package: cc.baka9.catseedlogin)
 *
 * -------------------------------------------------
 * Modifications and additional code
 * Copyright (c) 2026 Yueling
 * This work is licensed under the GNU GPL v3.0-or-later
 */

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