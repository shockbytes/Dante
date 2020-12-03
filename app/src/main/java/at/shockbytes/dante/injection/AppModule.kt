package at.shockbytes.dante.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.announcement.SharedPrefsAnnouncementProvider
import at.shockbytes.dante.signin.GoogleFirebaseSignInManager
import at.shockbytes.dante.signin.SignInManager
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.flagging.SharedPreferencesFeatureFlagging
import at.shockbytes.dante.suggestions.AssetsSuggestionsRepository
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.util.explanations.Explanations
import at.shockbytes.dante.util.explanations.SharedPrefsExplanations
import at.shockbytes.dante.util.permission.AndroidPermissionManager
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

/**
 * Author:  Martin Macheiner
 * Date:    13.02.2017
 */
@Module
class AppModule(private val app: Application) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    fun provideDanteSettings(
        sharedPreferences: SharedPreferences,
        schedulers: SchedulerFacade
    ): DanteSettings {
        return DanteSettings(app.applicationContext, sharedPreferences, schedulers)
    }

    @Provides
    fun providePermissionManager(): PermissionManager {
        return AndroidPermissionManager()
    }

    @Provides
    fun provideGoogleSignInManager(
        prefs: SharedPreferences,
        schedulers: SchedulerFacade
    ): SignInManager {
        return GoogleFirebaseSignInManager(prefs, app.applicationContext, schedulers)
    }

    @Provides
    fun provideFeatureFlagging(remoteConfig: FirebaseRemoteConfig): FeatureFlagging {
        return if (BuildConfig.DEBUG) {
            val prefs = app.getSharedPreferences("feature_flagging", Context.MODE_PRIVATE)
            SharedPreferencesFeatureFlagging(prefs)
        } else {
            FirebaseFeatureFlagging(remoteConfig)
        }
    }

    @Provides
    fun provideAnnouncementProvider(): AnnouncementProvider {
        val prefs = app.getSharedPreferences("announcements", Context.MODE_PRIVATE)
        return SharedPrefsAnnouncementProvider(prefs)
    }

    @Provides
    fun provideSuggestionsRepository(): SuggestionsRepository {
        return AssetsSuggestionsRepository(app.applicationContext, Gson())
    }

    @Provides
    fun provideExplanations(): Explanations {
        val sharedPreferences = app.getSharedPreferences("preferences_explanations", Context.MODE_PRIVATE)
        return SharedPrefsExplanations(sharedPreferences)
    }
}
