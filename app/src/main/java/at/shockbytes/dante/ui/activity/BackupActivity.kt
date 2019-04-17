package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackupFragment

class BackupActivity : ContainerBackNavigableActivity() {

    override val displayFragment: androidx.fragment.app.Fragment = BackupFragment.newInstance()

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BackupActivity::class.java)
        }
    }
}
