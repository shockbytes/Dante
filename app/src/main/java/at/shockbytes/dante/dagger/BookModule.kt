package at.shockbytes.dante.dagger

import android.app.Application
import android.content.SharedPreferences
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.books.RealmBookManager
import at.shockbytes.dante.network.BookDownloader
import dagger.Module
import dagger.Provides
import io.realm.Realm
import javax.inject.Singleton

/**
 * @author Martin Macheiner
 * Date: 14.01.2018.
 */

@Module
class BookModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideBookManager(bookDownloader: BookDownloader, realm: Realm,
                           prefs: SharedPreferences): BookManager {
        return RealmBookManager(bookDownloader, realm, app.applicationContext, prefs)
    }

}