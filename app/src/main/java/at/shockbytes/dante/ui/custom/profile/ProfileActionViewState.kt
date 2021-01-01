package at.shockbytes.dante.ui.custom.profile

sealed class ProfileActionViewState {

    object Hidden : ProfileActionViewState()

    /**
     * TODO allow usage of [showChangeImage] flag
     */
    data class Visible(
        val showUpgrade: Boolean,
        val showChangeName: Boolean,
        val showChangeImage: Boolean
    ) : ProfileActionViewState()

    companion object {

        fun forGoogleUser(): ProfileActionViewState {
            return Hidden
        }

        fun forMailUser(): ProfileActionViewState {
            return Visible(showUpgrade = false, showChangeName = true, showChangeImage = true)
        }

        fun forAnonymousUser(): ProfileActionViewState {
            return Visible(showUpgrade = true, showChangeName = true, showChangeImage = true)
        }
    }
}
