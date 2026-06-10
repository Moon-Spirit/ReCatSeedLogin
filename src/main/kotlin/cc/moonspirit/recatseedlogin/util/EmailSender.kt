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

package cc.moonspirit.recatseedlogin.util

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

object EmailSender {
    private val config = cc.moonspirit.recatseedlogin.bukkit.Config.EmailVerify

    fun sendEmail(receiveMailAccount: String?, subject: String?, content: String?) {
        if (receiveMailAccount.isNullOrEmpty()) {
            return
        }
        val properties = Properties()
        properties["mail.smtp.host"] = config.EmailSmtpHost
        try {
            properties["mail.smtp.port"] = config.EmailSmtpPort.toInt()
        } catch (e: NumberFormatException) {
            return
        }
        properties["mail.smtp.auth"] = "true"

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.EmailAccount, config.EmailPassword)
            }
        })

        configureSecurity(properties)

        try {
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(config.EmailAccount, config.FromPersonal))
            mimeMessage.setSubject(subject, "UTF-8")
            mimeMessage.setContent(content, "text/html; charset=UTF-8")
            mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(receiveMailAccount))
            Transport.send(mimeMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun configureSecurity(properties: Properties) {
        if (config.SSLAuthVerify) {
            properties["mail.smtp.ssl.enable"] = "true"
            properties["mail.smtp.ssl.check-server-identity"] = "true"
        } else {
            properties["mail.smtp.starttls.enable"] = "true"
        }
    }
}
