package at.shockbytes.dante

import android.os.StrictMode
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import at.shockbytes.dante.dagger.*
import at.shockbytes.dante.util.CrashlyticsReportingTree
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

/**
 * @author  Martin Macheiner
 * Date:    13.02.2017.
 */

class DanteApp : MultiDexApplication() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        setStrictMode()

        Realm.init(this)
        JodaTimeAndroid.init(this)

        configureFabric()
        configureLogging()

        appComponent = DaggerAppComponent.builder()
                .networkModule(NetworkModule())
                .bookModule(BookModule())
                .appModule(AppModule(this))
                .build()
    }

    private fun configureLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsReportingTree())
        }
    }

    private fun configureFabric() {

        // Configure Crashlytics anyway
        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics(), Answers())
                .debuggable(BuildConfig.DEBUG)
                .build())

        // to catch and send crash report to crashlytics when app crashes
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Timber.e(e, "uncaught exception")
            defaultExceptionHandler.uncaughtException(t, e)
        }
    }

    private fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }
    }

}
