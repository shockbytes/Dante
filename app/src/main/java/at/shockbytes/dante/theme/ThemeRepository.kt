package at.shockbytes.dante.theme

import io.reactivex.Single

interface ThemeRepository {

    fun getSeasonalTheme(): Single<SeasonalTheme>
}