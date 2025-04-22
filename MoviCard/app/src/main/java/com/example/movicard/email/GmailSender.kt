package com.example.movicard.email

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailSender(
    private val user: String,
    private val password: String
) {
    private val props: Properties = Properties()

    init {
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"
    }

    fun sendMail(subject: String, body: String, recipient: String) {
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(user, password)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(user))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            message.subject = subject
            message.setText(body)

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw RuntimeException("Error al enviar correo: ${e.message}")
        }
    }
}