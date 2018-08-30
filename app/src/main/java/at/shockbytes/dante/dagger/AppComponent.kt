package at.shockbytes.dante.dagger

import at.shockbytes.dante.ui.activity.*
import at.shockbytes.dante.ui.fragment.*
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SortStrategyDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.StatsDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SupporterBadgeDialogFragment
import dagger.Component
import javax.inject.Singleton

/**
 * @author Martin Macheiner
 * Date: 19.01.2017.
 */

@Singleton
@Component(modules = [
    (NetworkModule::class),
    (AppModule::class),
    (BookModule::class),
    (ViewModelModule::class),
    (FirebaseModule::class)
])
interface AppComponent {

    fun inject(activity: MainActivity)

    fun inject(activity: DetailActivity)

    fun inject(activity: BookRetrievalActivity)

    fun inject(activity: SearchActivity)

    fun inject(activity: BackupActivity)

    fun inject(fragment: MainBookFragment)

    fun inject(fragment: StatsDialogFragment)

    fun inject(fragment: DownloadBookFragment)

    fun inject(fragment: BackupFragment)

    fun inject(fragment: QueryCaptureFragment)

    fun inject(fragment: SearchFragment)

    fun inject(fragment: SuggestionsFragment)

    fun inject(fragment: BookDetailFragment)

    fun inject(fragment: MenuFragment)

    fun inject(fragment: ManualAddFragment)

    fun inject(dialogFragment: GoogleSignInDialogFragment)

    fun inject(dialogFragment: SortStrategyDialogFragment)

    fun inject(dialogFragment: SupporterBadgeDialogFragment)
}
