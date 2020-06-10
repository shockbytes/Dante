package at.shockbytes.dante.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsReportingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        FirebaseCrashlytics.getInstance().run {
            log(priority, tag, message)

            if (throwable != null && priority == Log.ERROR) {
                recordException(throwable)
            }
        }
    }
}