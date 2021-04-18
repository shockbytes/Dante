package at.shockbytes.dante.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import at.shockbytes.dante.announcement.AnnouncementProvider
import at.shockbytes.dante.announcement.SharedPrefsAnnouncementProvider
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.core.flagging.FeatureFlagging
import at.shockbytes.dante.flagging.FirebaseFeatureFlagging
import at.shockbytes.dante.core.flagging.SharedPreferencesFeatureFlagging
import at.shockbytes.dante.suggestions.SuggestionsRepository
import at.shockbytes.dante.suggestions.cache.DataStoreSuggestionsCache
import at.shockbytes.dante.suggestions.cache.SuggestionsCache
import at.shockbytes.dante.suggestions.firebase.FirebaseSuggestionsApi
import at.shockbytes.dante.suggestions.firebase.FirebaseSuggestionsRepository
import at.shockbytes.dante.theme.NoOpThemeRepository
import at.shockbytes.dante.theme.ThemeRepository
import at.shockbytes.dante.util.explanations.Explanations
import at.shockbytes.dante.util.explanations.SharedPrefsExplanations
import at.shockbytes.dante.util.permission.AndroidPermissionManager
import at.shockbytes.dante.util.permission.PermissionManager
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.tracking.Tracker
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
        loginRepository: LoginRepository,
        suggestionsCache: SuggestionsCache,
        tracker: Tracker
    ): SuggestionsRepository {
        return FirebaseSuggestionsRepository(
            firebaseSuggestionsApi,
            schedulerFacade,
            loginRepository,
            suggestionsCache,
            tracker
        )
    }

    @Provides
    fun provideThemeRepository(): ThemeRepository {
        return NoOpThemeRepository
    }

    @Provides
    fun provideExplanations(): Explanations {
        val sharedPreferences = app.getSharedPreferences("preferences_explanations", Context.MODE_PRIVATE)
        return SharedPrefsExplanations(sharedPreferences)
    }
}
