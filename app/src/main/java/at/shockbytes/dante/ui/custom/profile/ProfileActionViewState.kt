package at.shockbytes.dante.ui.custom.profile

sealed class ProfileActionViewState {

    object Hidden : ProfileActionViewState()

    data class Visible(
        val showUpgrade: Boolean,
        val showChangeName: Boolean,
        val showChangeImage: Boolean,
        val showChangePassword: Boolean
    ) : ProfileActionViewState()

    companion object {

        fun forGoogleUser(): ProfileActionViewState {
            return Hidden
        }

        fun forMailUser(): ProfileActionViewState {
            return Visible(
                showUpgrade = false,
                showChangeName = true,
                showChangeImage = true,
                showChangePassword = true
            )
        }

        fun forAnonymousUser(): ProfileActionViewState {
            return Visible(
                showUpgrade = true,
                showChangeName = true,
                showChangeImage = true,
                showChangePassword = false
            )
        }
    }
}
