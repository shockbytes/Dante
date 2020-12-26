package at.shockbytes.dante.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.announcement.SharedPrefsAnnouncementProvider
import at.shockbytes.dante.signin.GoogleFirebaseSignInRepository
import at.shockbytes.dante.signin.SignInRepository
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.flagging.SharedPreferencesFeatureFlagging
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.suggestions.cache.DataStoreSuggestionsCache
import at.shockbytes.dante.suggestions.cache.SuggestionsCache
import at.shockbytes.dante.suggestions.firebase.FirebaseSuggestionsApi
import at.shockbytes.dante.suggestions.firebase.FirebaseSuggestionsRepository
import at.shockbytes.dante.theme.FirebaseRemoteThemeRepository
import at.shockbytes.dante.theme.ThemeRepository
import at.shockbytes.dante.util.explanations.Explanations
import at.shockbytes.dante.util.explanations.SharedPrefsExplanations
import at.shockbytes.dante.util.permission.AndroidPermissionManager
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.tracking.Tracker
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
        schedulers: SchedulerFacade
    ): SignInRepository {
        return GoogleFirebaseSignInRepository(app.applicationContext, schedulers)
    }

    @Provides
    fun provideFeatureFlagging(remoteConfig: FirebaseRemoteConfig): FeatureFlagging {
        /**
         * Do not use [FirebaseFeatureFlagging] since there are no remotely controlled feature flags.
         */
        val prefs = app.getSharedPreferences("feature_flagging", Context.MODE_PRIVATE)
        return SharedPreferencesFeatureFlagging(prefs)
    }

    @Provides
    fun provideAnnouncementProvider(): AnnouncementProvider {
        val prefs = app.getSharedPreferences("announcements", Context.MODE_PRIVATE)
        return SharedPrefsAnnouncementProvider(prefs)
    }

    @Provides
    fun provideSuggestionCache(): SuggestionsCache {
        return DataStoreSuggestionsCache(app.applicationContext, Gson())
    }

    @Provides
    fun provideSuggestionsRepository(
        firebaseSuggestionsApi: FirebaseSuggestionsApi,
        schedulerFacade: SchedulerFacade,
        signInRepository: SignInRepository,
        suggestionsCache: SuggestionsCache,
        tracker: Tracker
    ): SuggestionsRepository {
        return FirebaseSuggestionsRepository(
            firebaseSuggestionsApi,
            schedulerFacade,
            signInRepository,
            suggestionsCache,
            tracker
        )
    }

    @Provides
    fun provideThemeRepository(
        firebaseRemoteConfig: FirebaseRemoteConfig
    ): ThemeRepository {
        return FirebaseRemoteThemeRepository(firebaseRemoteConfig, Gson())
    }

    @Provides
    fun provideExplanations(): Explanations {
        val sharedPreferences = app.getSharedPreferences("preferences_explanations", Context.MODE_PRIVATE)
        return SharedPrefsExplanations(sharedPreferences)
    }
}
