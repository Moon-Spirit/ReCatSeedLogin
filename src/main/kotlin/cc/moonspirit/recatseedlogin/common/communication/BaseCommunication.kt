package cc.moonspirit.recatseedlogin.common.communication

import cc.moonspirit.recatseedlogin.util.CommunicationAuth
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.Socket

public abstract class BaseCommunication {

    protected abstract fun getProxyHost(): String

    protected abstract fun getProxyPort(): Int

    protected abstract fun logError(message: String, e: Exception)

    protected abstract fun logWarning(message: String)

    public fun sendConnectRequest(playerName: String): Int {
        val socket = createSocket()
        try {
            val writer = createWriter(socket)
            writer.use {
                writeLine(it, "Connect")
                writeLine(it, playerName)
                it.flush()
            }
            return socket.getInputStream().read()
        } catch (e: IOException) {
            logError("Failed to send connect request for player: $playerName", e)
        } finally {
            socket?.close()
        }
        return 0
    }

    public fun sendKeepLoggedInRequest(playerName: String) {
        val socket = createSocket()
        try {
            val writer = createWriter(socket)
            writer.use {
                writeLine(it, "KeepLoggedIn")
                writeLine(it, playerName)
                val time = System.currentTimeMillis().toString()
                writeLine(it, time)
                val sign = CommunicationAuth.encryption(playerName, time, getAuthKey())
                writeLine(it, sign)
                it.flush()
            }
        } catch (e: IOException) {
            logError("Failed to send keep logged in request for player: $playerName", e)
        } finally {
            socket?.close()
        }
    }

    protected abstract fun getAuthKey(): String

    protected fun createSocket(): Socket {
        return try {
            Socket(getProxyHost(), getProxyPort())
        } catch (e: IOException) {
            logWarning("请检查装载登录插件的子服是否在配置文件中开启了代理功能，以及Host和Port是否与代理端的配置相同")
            throw IOException(e)
        }
    }

    protected fun createWriter(socket: Socket): BufferedWriter {
        return BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8))
    }

    private fun writeLine(writer: BufferedWriter?, line: String?) {
        try {
            if (writer == null || line == null) return
            writer.write(line)
            writer.newLine()
        } catch (e: IOException) {
            logError("Failed to write line", e)
        }
    }

    public fun close() {
        // No persistent connection to close in this implementation
    }
}
