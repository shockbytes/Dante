package at.shockbytes.dante.core;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import at.shockbytes.dante.dagger.AppComponent;
import at.shockbytes.dante.dagger.AppModule;
import at.shockbytes.dante.dagger.DaggerAppComponent;
import at.shockbytes.dante.dagger.NetworkModule;
import io.realm.Realm;

/**
 * @author Martin Macheiner
 *         Date: 13.02.2017.
 */

public class DanteApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        JodaTimeAndroid.init(this);

        appComponent = DaggerAppComponent.builder()
                .networkModule(new NetworkModule(this))
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
