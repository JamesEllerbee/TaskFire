package com.jamesellerbee.taskfire.tasktrackerapi.app.dal.stmp

import com.jamesellerbee.taskfire.tasktrackerapi.app.dal.properties.ApplicationProperties
import com.jamesellerbee.tasktracker.lib.util.ResolutionStrategy
import com.jamesellerbee.tasktracker.lib.util.ServiceLocator
import org.apache.commons.mail.SimpleEmail

class GoogleSmtpEmailSender(serviceLocator: ServiceLocator) {
    private val applicationProperties by serviceLocator.resolveLazy<ApplicationProperties>(
        ResolutionStrategy.ByType(
            type = ApplicationProperties::class
        )
    )

    fun sendVerificationEmail(recipientEmail: String, verificationLink: String) {
        val email = getSimpleEmail()
        email.addTo(recipientEmail)
        email.subject = "Welcome to Taskfire!"
        email.setMsg("Click the following link to get verified and start tracking tasks.\n\n$verificationLink")
        email.send()
    }

    fun sendResetEmail(recipientEmail: String, resetLink: String) {
        val email = getSimpleEmail()
        email.addTo(recipientEmail)
        email.subject = "Reset password"
        email.setMsg("Click the following link to reset your password and gain access to your account.\n\n$resetLink\n\nDidn't request this? Ignore this email and no further action is required.")
        email.send()
    }

    private fun getSimpleEmail(): SimpleEmail {
        val account = applicationProperties["emailAccount"] as String
        val password = applicationProperties["emailPassword"] as String
        val alias = applicationProperties["alias"] as String

        return SimpleEmail().also {
            it.hostName = "smtp.googlemail.com"
            it.setSmtpPort(465)

            it.setAuthentication(
                account,
                password
            )

            it.setSSLOnConnect(true)
            it.setStartTLSRequired(true)
            it.setFrom(alias)
        }
    }
}