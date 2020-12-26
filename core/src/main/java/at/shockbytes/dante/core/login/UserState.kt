package at.shockbytes.dante.core.login

sealed class UserState {

    data class SignedInUser(val user: DanteUser) : UserState()

    object Unauthenticated : UserState()
}
