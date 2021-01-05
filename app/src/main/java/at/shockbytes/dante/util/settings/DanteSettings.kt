package at.shockbytes.dante.util.settings

import android.content.Context
import android.content.SharedPreferences
import at.shockbytes.dante.R
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.settings.delegate.boolDelegate
import at.shockbytes.dante.util.settings.delegate.stringDelegate
import at.shockbytes.dante.util.sort.SortStrategy
import at.shockbytes.dante.util.sort.TimeLineSortStrategy
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable

/**
 * Author:  Martin Macheiner
 * Date:    11.02.2018
 */
class DanteSettings(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val schedulers: SchedulerFacade
) {

    private val rxPrefs: RxSharedPreferences = RxSharedPreferences.create(prefs)

    var isNewUser: Boolean by prefs.boolDelegate(context.getString(R.string.prefs_is_new_user))

    // This field will only be updated if the next "user sessions" starts.
    var isFirstUserSession: Boolean = isNewUser

    var hasUserSeenOnboardingHints: Boolean by prefs.boolDelegate(context.getString(R.string.prefs_onboarding_hints))

    private val darkModeString: String by prefs.stringDelegate(context.getString(R.string.prefs_dark_mode_key), defaultValue = "light")

    var showRandomPickInteraction: Boolean by prefs.boolDelegate(context.getString(R.string.prefs_pick_random_key))

    val themeState: ThemeState
        get() = ThemeState.ofString(darkModeString) ?: ThemeState.SYSTEM

    var showSummary: Boolean by prefs.boolDelegate(context.getString(R.string.prefs_show_summary_key))

    var trackingEnabled: Boolean by prefs.boolDelegate(context.getString(R.string.prefs_tracking_key), defaultValue = true)

    var sortStrategy: SortStrategy
        get() {
            val ordinal = prefs.getInt(context.getString(R.string.prefs_sort_strategy_key), 0)
            return SortStrategy.values()[ordinal]
        }
        set(value) {
            prefs.edit()
                    .putInt(context.getString(R.string.prefs_sort_strategy_key), value.ordinal)
                    .apply()
        }

    var timeLineSortStrategy: TimeLineSortStrategy
        get() {
            val ordinal = prefs.getInt(context.getString(R.string.prefs_timeline_sort_strategy_key), 0)
            return TimeLineSortStrategy.values()[ordinal]
        }
        set(value) {
            prefs.edit()
                .putInt(context.getString(R.string.prefs_timeline_sort_strategy_key), value.ordinal)
                .apply()
        }

    fun observeSortStrategy(): Observable<SortStrategy> {
        return rxPrefs.getInteger(context.getString(R.string.prefs_sort_strategy_key))
                .asObservable()
                .map { ordinal ->
                    SortStrategy.values()[ordinal]
                }
                .subscribeOn(schedulers.computation)
                .observeOn(schedulers.ui)
    }

    fun observeThemeChanged(): Observable<ThemeState> {
        return rxPrefs.getString(context.getString(R.string.prefs_dark_mode_key))
            .asObservable()
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .map(ThemeState.Companion::ofStringWithDefault)
            .subscribeOn(schedulers.computation)
            .observeOn(schedulers.ui)
    }

    fun observeRandomPickInteraction(): Observable<Boolean> {
        return rxPrefs.getBoolean(context.getString(R.string.prefs_pick_random_key))
                .asObservable()
                .distinctUntilChanged()
                .subscribeOn(schedulers.computation)
                .observeOn(schedulers.ui)
    }
}