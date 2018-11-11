package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.backup.BackupServiceConnectionException
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackupFragment
import timber.log.Timber
import javax.inject.Inject


class BackupActivity : ContainerBackNavigableActivity() {

    override val displayFragment: Fragment = BackupFragment.newInstance()

    @Inject
    protected lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            backupManager.connect(this)
        } catch (e: BackupServiceConnectionException) {
            Timber.e(e)
            showToast(R.string.backup_connection_error_google_drive, showLong = true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backupManager.close()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BackupActivity::class.java)
        }
    }


}
