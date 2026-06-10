package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent

class ProtocolLibListeners : PacketAdapter(CatSeedLogin.instance, ListenerPriority.HIGHEST,
    PacketType.Play.Server.SET_SLOT,
    PacketType.Play.Server.WINDOW_ITEMS
) {

    override fun onPacketSending(event: PacketEvent) {
        val packetType = event.packetType
        if (packetType == PacketType.Play.Server.SET_SLOT || packetType == PacketType.Play.Server.WINDOW_ITEMS) {
            val player = event.player
            val packet = event.packet
            val windowId = packet.integers.read(0)
            if (windowId == 0 && !LoginPlayerHelper.isLogin(player.name)) {
                event.isCancelled = true
            }
        }
    }

    override fun onPacketReceiving(event: PacketEvent) {
        // No-op
    }

    companion object {
        @JvmStatic
        fun enable() {
            val protocolManager = ProtocolLibrary.getProtocolManager()
            protocolManager.addPacketListener(ProtocolLibListeners())
        }
    }
}