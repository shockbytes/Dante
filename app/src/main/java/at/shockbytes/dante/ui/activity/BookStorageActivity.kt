package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import at.shockbytes.dante.injection.AppComponent
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.activity.core.BackNavigableActivity
import at.shockbytes.dante.ui.fragment.BackupFragment
import at.shockbytes.dante.ui.fragment.ImportBooksStorageFragment
import at.shockbytes.dante.ui.fragment.OnlineStorageFragment
import javax.inject.Inject
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_book_storage.*
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class BookStorageActivity : BackNavigableActivity(), EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_storage)
        viewModel = viewModelOf(vmFactory)

        initializeNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_book_storage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_book_storage_delete_library) {
            confirmLibraryDeletion()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun confirmLibraryDeletion() {
        MaterialDialog(this).show {
            icon(R.drawable.ic_burn)
            title(text = getString(R.string.ask_for_library_deletion))
            message(text = getString(R.string.ask_for_library_deletion_msg))
            positiveButton(R.string.action_delete) {
                deleteLibrary()
            }
            negativeButton(android.R.string.no) {
                dismiss()
            }
            cancelOnTouchOutside(false)
            cornerRadius(AppUtils.convertDpInPixel(6, this@BookStorageActivity).toFloat())
        }
    }

    private fun deleteLibrary() {
        viewModel.deleteLibrary()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showSnackbar(getString(R.string.library_deletion_succeeded))
            }, { throwable ->
                Timber.e(throwable)
                showSnackbar(getString(R.string.library_deletion_failed))
            })
            .addTo(compositeDisposable)
    }

    private fun initializeNavigation() {

        bottom_navigation_book_storage.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.menu_book_storage_online -> {
                    setActionBarElevation(AppUtils.convertDpInPixel(4, this).toFloat())
                    showFragment(OnlineStorageFragment.newInstance())
                }
                R.id.menu_book_storage_local -> {
                    setActionBarElevation(0f)
                    showFragment(BackupFragment.newInstance())
                }
                R.id.menu_book_storage_import -> {
                    setActionBarElevation(AppUtils.convertDpInPixel(4, this).toFloat())
                    showFragment(ImportBooksStorageFragment.newInstance())
                }
            }

            true
        }

        bottom_navigation_book_storage.selectedItemId = R.id.menu_book_storage_local
    }

    private fun setActionBarElevation(elevation: Float) {
        supportActionBar?.elevation = elevation
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
