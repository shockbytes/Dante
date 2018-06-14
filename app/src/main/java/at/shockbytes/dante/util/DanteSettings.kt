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
 * @author Martin Macheiner
 * Date: 11.02.2018.
 */

class DanteSettings(private val context: Context,
                    private val prefs: SharedPreferences) {

    private val rxPrefs: RxSharedPreferences = RxSharedPreferences.create(prefs)

    var pageTrackingEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_page_tracking_key), true)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_page_tracking_key), value)
                    .apply()
        }

    var pageOverlayEnabled: Boolean
        get() = prefs.getBoolean(context.getString(R.string.prefs_page_overlay_key), true)
        set(value) {
            prefs.edit()
                    .putBoolean(context.getString(R.string.prefs_page_overlay_key), value)
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

}