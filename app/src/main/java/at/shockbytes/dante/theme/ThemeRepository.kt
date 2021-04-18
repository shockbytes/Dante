package at.shockbytes.dante.theme

import io.reactivex.rxjava3.core.Single

/**
 * Example for remote data:
 * {
 *   "name": "Winter Season",
 *   "type": "lottie_assets",
 *   "resource": "lottie_illustration_snow.json",
 *   "resource_speed": 0.5
}
 */
interface ThemeRepository {

    fun getSeasonalTheme(): Single<SeasonalTheme>
}