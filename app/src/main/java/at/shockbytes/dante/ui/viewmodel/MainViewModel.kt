package at.shockbytes.dante.ui.viewmodel

import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.theme.SeasonalTheme
import at.shockbytes.dante.theme.ThemeRepository
import at.shockbytes.dante.util.ExceptionHandlers
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.settings.DanteSettings
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    10.06.2018
 */
class MainViewModel @Inject constructor(
    private val announcementProvider: AnnouncementProvider,
    private val danteSettings: DanteSettings,
    private val themeRepository: ThemeRepository
) : BaseViewModel() {

    sealed class MainEvent {

        object Announcement : MainEvent()
    }

    private val eventSubject = PublishSubject.create<MainEvent>()
    fun onMainEvent(): Observable<MainEvent> = eventSubject

    private val seasonalThemeSubject = BehaviorSubject.create<SeasonalTheme>()
    fun getSeasonalTheme(): Observable<SeasonalTheme> = seasonalThemeSubject
        .delay(2, TimeUnit.SECONDS)
        .distinctUntilChanged()

    fun requestSeasonalTheme() {
        themeRepository.getSeasonalTheme()
            .doOnError { seasonalThemeSubject.onNext(SeasonalTheme.NoTheme) }
            .subscribe(seasonalThemeSubject::onNext, ExceptionHandlers::defaultExceptionHandler)
            .addTo(compositeDisposable)
    }

    fun queryAnnouncements() {
        val hasActiveAnnouncement = announcementProvider.getActiveAnnouncement() != null
        // Do not show announcements if the user first logs into the app,
        // even though there would be a new announcement
        val showAnnouncement = hasActiveAnnouncement && !danteSettings.isFirstUserSession
        if (showAnnouncement) {
            eventSubject.onNext(MainEvent.Announcement)
        }
    }
}