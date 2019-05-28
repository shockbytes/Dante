package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackupFragment
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

class BackupActivity : ContainerBackNavigableActivity(), EasyPermissions.PermissionCallbacks {

    override val displayFragment = BackupFragment.newInstance()

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) = Unit

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Reload data sources once external permission is granted
        viewModel.connect(this)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BackupActivity::class.java)
        }
    }
}
