package at.shockbytes.dante.dagger

import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.data.RealmBookEntityDao
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.core.network.google.GoogleBooksApi
import at.shockbytes.dante.core.network.google.GoogleBooksDownloader
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Author:  Martin Macheiner
 * Date:    14.01.2018
 */
@Module
class BookModule {

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
}