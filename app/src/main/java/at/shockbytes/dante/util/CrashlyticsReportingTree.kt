package at.shockbytes.dante.util

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class CrashlyticsReportingTree: Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Crashlytics.log(priority, tag, message)

        t?.let { throwable ->
            if (priority == Log.ERROR) {
                Crashlytics.logException(throwable)
            }
        }
    }
}