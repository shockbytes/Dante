package at.shockbytes.dante.util

import android.content.Context
import android.content.SharedPreferences
import at.shockbytes.dante.R
import at.shockbytes.dante.util.sort.SortStrategy
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Author:  Martin Macheiner
 * Date:    11.02.2018
 */
class DanteSettings(
    private val context: Context,
    private val prefs: SharedPreferences
) {

    private val rxPrefs: RxSharedPreferences = RxSharedPreferences.create(prefs)

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_dark_mode_key), false)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_dark_mode_key), value)
                    .apply()
        }

    var showSummary: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_show_summary_key), true)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_show_summary_key), value)
                    .apply()
        }

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

    fun observeSortStrategy(): Observable<SortStrategy> {
        return rxPrefs.getInteger(context.getString(R.string.prefs_sort_strategy_key))
                .asObservable()
                .map { ordinal ->
                    SortStrategy.values()[ordinal]
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun observeDarkModeEnabled(): Observable<Boolean> {
        return rxPrefs.getBoolean(context.getString(R.string.prefs_dark_mode_key))
                .asObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }
}