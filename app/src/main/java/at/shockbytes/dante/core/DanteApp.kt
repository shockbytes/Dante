package at.shockbytes.dante.core

import android.app.Application
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.dagger.*
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import net.danlew.android.joda.JodaTimeAndroid

/**
 * @author Martin Macheiner
 * Date: 13.02.2017.
 */

class DanteApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        JodaTimeAndroid.init(this)

        if (BuildConfig.DEBUG) {
            Fabric.with(Fabric.Builder(this)
                    .kits(Crashlytics())
                    .debuggable(BuildConfig.DEBUG)
                    .build())
        }

        appComponent = DaggerAppComponent.builder()
                .networkModule(NetworkModule())
                .bookModule(BookModule(this))
                .appModule(AppModule(this))
                .build()
    }
}
