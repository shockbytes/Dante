package at.shockbytes.dante.dagger

import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.activity.DownloadActivity
import at.shockbytes.dante.ui.activity.MainActivity
import at.shockbytes.dante.ui.fragment.BackupFragment
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.ui.fragment.MainBookFragment
import at.shockbytes.dante.ui.fragment.dialogs.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialogs.StatsDialogFragment
import at.shockbytes.dante.util.barcode.QueryCaptureActivity
import dagger.Component
import javax.inject.Singleton

/**
 * @author Martin Macheiner
 * Date: 19.01.2017.
 */

@Singleton
@Component(modules = [(NetworkModule::class), (AppModule::class)])
interface AppComponent {

    fun inject(activity: MainActivity)

    fun inject(activity: DetailActivity)

    fun inject(activity: QueryCaptureActivity)

    fun inject(activity: DownloadActivity)

    fun inject(fragment: MainBookFragment)

    fun inject(fragment: StatsDialogFragment)

    fun inject(fragment: DownloadBookFragment)

    fun inject(fragment: BackupFragment)

    fun inject(dialogFragment: GoogleSignInDialogFragment)

}
