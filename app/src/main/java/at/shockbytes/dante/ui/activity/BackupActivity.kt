package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackupFragment


class BackupActivity : ContainerBackNavigableActivity(), BackupFragment.OnBackupRestoreListener {

    override val displayFragment: Fragment = BackupFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED, Intent())
    }

    override fun onBackupRestored() {
        setResult(RESULT_OK, Intent())
    }

    companion object {

        const val rcBackupRestored = 0x7592

        fun newIntent(context: Context): Intent {
            return Intent(context, BackupActivity::class.java)
        }
    }


}
