package at.shockbytes.dante.core.injection

import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.data.BookEntityDao
import at.shockbytes.dante.core.data.DanteRealmMigration
import at.shockbytes.dante.core.data.RealmBookEntityDao
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.core.network.google.GoogleBooksApi
import at.shockbytes.dante.core.network.google.GoogleBooksDownloader
import at.shockbytes.dante.util.scheduler.AppSchedulerFacade
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import dagger.Module
import dagger.Provides
import io.realm.RealmConfiguration
import javax.inject.Singleton

@Module
class CoreModule {

    @Provides
    @Singleton
    fun provideBookDao(realm: RealmInstanceProvider): BookEntityDao {
        return RealmBookEntityDao(realm)
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
            .migration(DanteRealmMigration())
            .build())
    }

    @Provides
    @Singleton
    fun provideSchedulerFacade(): SchedulerFacade {
        return AppSchedulerFacade()
    }
}