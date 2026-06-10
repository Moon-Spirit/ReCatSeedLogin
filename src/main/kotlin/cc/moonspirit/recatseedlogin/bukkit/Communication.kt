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

package cc.moonspirit.recatseedlogin.bukkit

import cc.moonspirit.recatseedlogin.bukkit.objects.LoginPlayerHelper
import cc.moonspirit.recatseedlogin.common.communication.BaseCommunication
import cc.moonspirit.recatseedlogin.common.model.LoginPlayer
import cc.moonspirit.recatseedlogin.util.CommunicationAuth
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class Communication : BaseCommunication() {

    companion object {
        private var serverSocket: ServerSocket? = null

        @JvmStatic
        fun socketServerStopAsync() {
            CatScheduler.runTaskAsync { socketServerStop() }
        }

        @JvmStatic
        fun socketServerStop() {
            val sock = serverSocket
            if (sock != null && !sock.isClosed) {
                try {
                    sock.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun socketServerStartAsync() {
            CatScheduler.runTaskAsync { socketServerStart() }
        }

        @JvmStatic
        fun socketServerStart() {
            try {
                val port = PluginContext.getConfigManager()?.getProxyPort() ?: 0
                serverSocket = ServerSocket(port, 50)
                while (serverSocket?.isClosed == false) {
                    acceptAndHandle()
                }
            } catch (e: IOException) {
                PluginContext.getLogger().warning("无法启动Socket服务器: " + e.message)
                e.printStackTrace()
            }
        }

        private fun acceptAndHandle() {
            val sock = serverSocket?.accept() ?: return
            try {
                sock.use { handleRequest(it) }
            } catch (e: IOException) {
                if (serverSocket?.isClosed == false) {
                    PluginContext.getLogger().warning("Socket连接处理异常: " + e.message)
                }
            }
        }

        private fun handleRequest(socket: Socket) {
            BufferedReader(InputStreamReader(socket.getInputStream())).use { bufferedReader ->
                socket.getOutputStream().use { outputStream ->
                    val requestType = bufferedReader.readLine()
                    val playerName = bufferedReader.readLine()
                    when (requestType) {
                        "Connect" -> handleConnectRequest(outputStream, playerName)
                        "KeepLoggedIn" -> {
                            val time = bufferedReader.readLine()
                            val sign = bufferedReader.readLine()
                            handleKeepLoggedInRequest(playerName, time, sign)
                        }
                        else -> {
                            // unknown request type, ignore
                        }
                    }
                }
            }
        }

        private fun handleKeepLoggedInRequest(playerName: String?, time: String?, sign: String?) {
            val expectedSign = CommunicationAuth.encryption(
                playerName ?: "", time ?: "", PluginContext.getConfigManager()?.getAuthKey() ?: ""
            )
            if (sign != expectedSign) return

            CatScheduler.runTask {
                val lp: LoginPlayer? = Cache.getIgnoreCase(playerName ?: return@runTask)
                if (lp == null) return@runTask
                LoginPlayerHelper.add(lp)
                val player: Player? = Bukkit.getPlayerExact(playerName)
                if (player != null) {
                    player.updateInventory()
                }
            }
        }

        private fun handleConnectRequest(outputStream: OutputStream, playerName: String?) {
            CatScheduler.runTask {
                val result = LoginPlayerHelper.isLogin(playerName ?: return@runTask)
                try {
                    outputStream.write(if (result) 1 else 0)
                    outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getProxyHost(): String = PluginContext.getConfigManager()?.getProxyHost() ?: ""

    override fun getProxyPort(): Int = PluginContext.getConfigManager()?.getProxyPort() ?: 0

    override fun getAuthKey(): String = PluginContext.getConfigManager()?.getAuthKey() ?: ""

    override fun logError(message: String, e: Exception) {
        PluginContext.getLogger().severe(message)
        e.printStackTrace()
    }

    override fun logWarning(message: String) {
        PluginContext.getLogger().warning(message)
    }
}