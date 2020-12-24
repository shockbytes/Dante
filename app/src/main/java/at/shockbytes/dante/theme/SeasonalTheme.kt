package at.shockbytes.dante.theme

sealed class SeasonalTheme {

    data class LottieAssetsTheme(
        val lottieAsset: String,
        val lottieSpeed: Float
    ) : SeasonalTheme()

    object NoTheme : SeasonalTheme()

    companion object {
        const val RESOURCE_TYPE_LOTTIE_ASSETS = "lottie_assets"
    }
}
