package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import at.shockbytes.dante.injection.AppComponent
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.BackupFragment
import at.shockbytes.dante.ui.fragment.ImportBooksStorageFragment
import at.shockbytes.dante.ui.fragment.OnlineStorageFragment
import javax.inject.Inject
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.activity_book_storage.*
import kotlinx.android.synthetic.main.dante_toolbar.*
import pub.devrel.easypermissions.EasyPermissions

class BookStorageActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_storage)
        viewModel = viewModelOf(vmFactory)

        initializeNavigation()
        hideActionBar()
        setupToolbar()
    }

    private fun setupToolbar() {
        dante_toolbar_title.setText(R.string.label_book_storage)
        dante_toolbar_back.apply {
            setVisible(true)
            setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun initializeNavigation() {

        bottom_navigation_book_storage.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.menu_book_storage_online -> {
                    showFragment(OnlineStorageFragment.newInstance())
                }
                R.id.menu_book_storage_local -> {
                    showFragment(BackupFragment.newInstance())
                }
                R.id.menu_book_storage_import -> {
                    showFragment(ImportBooksStorageFragment.newInstance())
                }
            }

            true
        }

        bottom_navigation_book_storage.selectedItemId = R.id.menu_book_storage_local
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_book_storage, fragment)
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) = Unit

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Reload data sources once external permission is granted
        viewModel.connect(this, forceReload = true)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BookStorageActivity::class.java)
        }
    }
}
