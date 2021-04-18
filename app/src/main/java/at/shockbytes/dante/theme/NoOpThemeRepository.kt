package at.shockbytes.dante.theme

import io.reactivex.rxjava3.core.Single

/**
 * Dummy implementation when no theme is used anyways.
 */
object NoOpThemeRepository : ThemeRepository {
    override fun getSeasonalTheme(): Single<SeasonalTheme> = Single.just(SeasonalTheme.NoTheme)
}