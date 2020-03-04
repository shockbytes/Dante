package at.shockbytes.dante.signin

sealed class UserState {

    data class SignedInUser(val user: DanteUser) : UserState()

    object AnonymousUser : UserState()
}
