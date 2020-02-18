package at.shockbytes.dante

import android.os.StrictMode
import androidx.multidex.MultiDexApplication
import androidx.appcompat.app.AppCompatDelegate
import at.shockbytes.dante.core.injection.CoreComponent
import at.shockbytes.dante.core.injection.CoreComponentProvider
import at.shockbytes.dante.core.injection.CoreModule
import at.shockbytes.dante.core.injection.DaggerCoreComponent
import at.shockbytes.dante.core.injection.NetworkModule
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.AppModule
import at.shockbytes.dante.injection.AppNetworkModule
import at.shockbytes.dante.injection.DaggerAppComponent
import at.shockbytes.dante.injection.FirebaseModule
import at.shockbytes.dante.util.CrashlyticsReportingTree
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import io.realm.Realm
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
class DanteApp : MultiDexApplication(), CoreComponentProvider {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val coreComponent: CoreComponent by lazy {
        DaggerCoreComponent.builder()
            .coreModule(CoreModule())
            .networkModule(NetworkModule(this))
            .build()
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
        configureRxJavaErrorHandling()

        appComponent = DaggerAppComponent.builder()
            .appNetworkModule(AppNetworkModule())
            .appModule(AppModule(this))
            .firebaseModule(FirebaseModule(this))
            .coreComponent(provideCoreComponent())
            .build()
    }

    override fun provideCoreComponent(): CoreComponent {
        return coreComponent
    }

    private fun configureRxJavaErrorHandling() {
        RxJavaPlugins.setErrorHandler { throwable ->
            Timber.e(throwable)
        }
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
            defaultExceptionHandler?.uncaughtException(t, e)
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
