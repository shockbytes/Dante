package at.shockbytes.dante.theme

import io.reactivex.Single

/**
 * Dummy implementation when no theme is used anyways.
 */
object NoOpThemeRepository : ThemeRepository {
    override fun getSeasonalTheme(): Single<SeasonalTheme> = Single.just(SeasonalTheme.NoTheme)
}