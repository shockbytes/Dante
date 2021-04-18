package at.shockbytes.dante.core.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.data.BookLabelDao
import at.shockbytes.dante.core.data.BookLabelRepository
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.DefaultBookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.core.data.ReadingGoalRepository
import at.shockbytes.dante.core.data.local.DanteRealmMigration
import at.shockbytes.dante.core.data.DefaultBookLabelRepository
import at.shockbytes.dante.core.data.local.RealmBookEntityDao
import at.shockbytes.dante.core.data.local.RealmBookLabelDao
import at.shockbytes.dante.core.data.local.RealmPageRecordDao
import at.shockbytes.dante.core.data.local.SharedPrefsBackedReadingGoalRepository
import at.shockbytes.dante.core.data.warehouse.WarehouseBookLabelDao
import at.shockbytes.dante.core.data.warehouse.WarehouseBookEntityDao
import at.shockbytes.dante.core.flagging.FeatureFlag
import at.shockbytes.dante.core.flagging.FeatureFlagging
import at.shockbytes.dante.core.flagging.SharedPreferencesFeatureFlagging
import at.shockbytes.dante.core.image.GlideImageLoader
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.image.picker.DefaultImagePicking
import at.shockbytes.dante.core.image.picker.ImagePickerConfig
import at.shockbytes.dante.core.image.picker.ImagePicking
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.core.network.google.GoogleBooksApi
import at.shockbytes.dante.core.network.google.GoogleBooksDownloader
import at.shockbytes.dante.core.user.FirebaseUserRepository
import at.shockbytes.dante.core.user.UserRepository
import at.shockbytes.dante.util.scheduler.AppSchedulerFacade
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.warehouse.Warehouse
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

@Module
class CoreModule(
    private val app: Application,
    private val config: CoreModuleConfig
) {

    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
    }

    @Provides
    @Singleton
    @Named(LOCAL_BOOK_DAO)
    fun provideBookDao(realm: RealmInstanceProvider): BookEntityDao {
        return RealmBookEntityDao(realm)
    }

    @Provides
    @Singleton
    fun provideBookLabelDao(realm: RealmInstanceProvider): BookLabelDao {
        return RealmBookLabelDao(realm)
    }

    @Provides
    @Singleton
    fun providePageRecordDao(realm: RealmInstanceProvider): PageRecordDao {
        return RealmPageRecordDao(realm)
    }

    @Provides
    @Singleton
    @Named(READING_GOAL_SHARED_PREFERENCES)
    fun provideReadingGoalSharedPreferences(): SharedPreferences {
        return app.getSharedPreferences(READING_GOAL_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideReadingGoalRepository(
        @Named(READING_GOAL_SHARED_PREFERENCES) sharedPreferences: SharedPreferences,
        schedulerFacade: SchedulerFacade
    ): ReadingGoalRepository {
        return SharedPrefsBackedReadingGoalRepository(sharedPreferences, schedulerFacade)
    }

    @Provides
    fun provideFeatureFlagging(): FeatureFlagging {
        /**
         * Do not use [FirebaseFeatureFlagging] since there are no remotely controlled feature flags.
         */
        val prefs = app.getSharedPreferences("feature_flagging", Context.MODE_PRIVATE)
        return SharedPreferencesFeatureFlagging(prefs)
    }

    @Provides
    @Singleton
    fun provideBookLabelRepository(
        bookLabelDao: BookLabelDao,
        bookLabelWarehouse: Warehouse<BookLabel>,
        featureFlagging: FeatureFlagging
    ): BookLabelRepository {
        val dao =  if (featureFlagging[FeatureFlag.FireFlash]) {
            WarehouseBookLabelDao(bookLabelWarehouse)
        } else {
            bookLabelDao
        }
        return DefaultBookLabelRepository(dao)
    }

    @Provides
    @Singleton
    fun provideBookRepository(
        @Named(LOCAL_BOOK_DAO) localBookDao: BookEntityDao,
        bookWarehouse: Warehouse<BookEntity>,
        featureFlagging: FeatureFlagging
    ): BookRepository {
        val dao = if (featureFlagging[FeatureFlag.FireFlash]) {
            WarehouseBookEntityDao(bookWarehouse)
        } else {
            localBookDao
        }
        return DefaultBookRepository(dao)
    }

    @Provides
    @Singleton
    fun provideBookDownloader(
        api: GoogleBooksApi,
        schedulerFacade: SchedulerFacade
    ): BookDownloader {
        return GoogleBooksDownloader(api, schedulerFacade)
    }

    @Provides
    @Singleton
    fun provideRealmInstanceProvider(): RealmInstanceProvider {
        return RealmInstanceProvider(RealmConfiguration.Builder()
            .schemaVersion(DanteRealmMigration.migrationVersion)
            .allowWritesOnUiThread(config.allowRealmExecutionOnUiThread)
            .allowQueriesOnUiThread(config.allowRealmExecutionOnUiThread)
            .migration(DanteRealmMigration())
            .build())
    }

    @Provides
    @Singleton
    fun provideSchedulerFacade(): SchedulerFacade {
        return AppSchedulerFacade()
    }

    @Provides
    @Singleton
    fun provideImageLoader(): ImageLoader {
        return GlideImageLoader
    }

    @Provides
    @Singleton
    fun provideImagePicker(): ImagePicking {
        return DefaultImagePicking(
            ImagePickerConfig(
                maxSize = 1024,
                maxHeight = 1280,
                maxWidth = 720
            )
        )
    }

    @Provides
    fun provideUserRepository(
        fbAuth: FirebaseAuth,
        schedulers: SchedulerFacade
    ): UserRepository {
        return FirebaseUserRepository(fbAuth, schedulers)
    }

    companion object {

        private const val LOCAL_BOOK_DAO = "local_book_dao"
        private const val READING_GOAL_SHARED_PREFERENCES = "reading_goal_shared_preferences"
    }

    data class CoreModuleConfig(
        val allowRealmExecutionOnUiThread: Boolean
    )
}