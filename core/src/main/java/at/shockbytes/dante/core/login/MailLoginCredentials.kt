package at.shockbytes.dante.core.login

data class MailLoginCredentials(
    val address: String,
    val password: String,
    val isSignUp: Boolean
)