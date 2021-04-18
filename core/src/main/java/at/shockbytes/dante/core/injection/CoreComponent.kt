package at.shockbytes.dante.core.injection

import android.content.SharedPreferences
import at.shockbytes.dante.core.book.realm.RealmInstanceProvider
import at.shockbytes.dante.core.data.BookRepository
import at.shockbytes.dante.core.data.PageRecordDao
import at.shockbytes.dante.core.data.ReadingGoalRepository
import at.shockbytes.dante.core.flagging.FeatureFlagging
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.image.picker.ImagePicking
import at.shockbytes.dante.core.login.GoogleAuth
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.core.network.BookDownloader
import at.shockbytes.dante.core.network.google.GoogleBooksApi
import at.shockbytes.dante.core.user.UserRepository
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.firebase.auth.FirebaseAuth
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Component(
    modules = [
        CoreModule::class,
        NetworkModule::class,
        LoginModule::class
    ]
)
@Singleton
interface CoreComponent {

    fun getBookRepository(): BookRepository
    fun getPageRecordDao(): PageRecordDao
    fun getBookDownloader(): BookDownloader
    fun getRealmInstanceProvider(): RealmInstanceProvider
    fun getReadingGoalRepository(): ReadingGoalRepository

    fun getGoogleAuth(): GoogleAuth
    fun getLoginRepository(): LoginRepository
    fun getUserRepository(): UserRepository
    fun getFirebaseAuth(): FirebaseAuth

    fun provideFeatureFlagging(): FeatureFlagging

    fun getImageLoader(): ImageLoader
    fun getImagePicker(): ImagePicking
    fun getSchedulerFacade(): SchedulerFacade

    fun getOkHttpClient(): OkHttpClient
    fun provideGoogleBooksApi(): GoogleBooksApi
}
