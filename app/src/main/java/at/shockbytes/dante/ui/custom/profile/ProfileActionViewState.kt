package at.shockbytes.dante.ui.custom.profile

sealed class ProfileActionViewState {

    object Hidden : ProfileActionViewState()

    data class Visible(
        val showUpgrade: Boolean,
        val showChangeName: Boolean,
        val showChangeImage: Boolean
    ): ProfileActionViewState()
}
