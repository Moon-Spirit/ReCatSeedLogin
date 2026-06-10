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

class CatSeedPlayerRegisterEvent(val player: Player) : Event() {

    override fun getHandlers(): HandlerList = handlers

    companion object {
        @JvmField
        val handlers: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlers
    }
}