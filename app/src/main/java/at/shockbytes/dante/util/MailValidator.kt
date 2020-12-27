package at.shockbytes.dante.util

import java.util.regex.Pattern

object MailValidator {

    private const val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    private val pattern = Pattern.compile(EMAIL_PATTERN)

    fun validateMail(email: CharSequence): Boolean {
        return pattern.matcher(email).matches()
    }
}