package at.shockbytes.dante.theme

sealed class SeasonalTheme {

    data class LottieTheme(val idName: String) : SeasonalTheme()

    object NoTheme : SeasonalTheme()

    companion object {
        const val RESOURCE_TYPE_LOTTIE = "lottie"
    }
}
