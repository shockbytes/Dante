package at.shockbytes.dante.ui.activity

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import at.shockbytes.dante.R
import at.shockbytes.dante.camera.BarcodeScanResultBottomSheetDialogFragment
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.fragment.SearchFragment
import at.shockbytes.dante.ui.viewmodel.SearchViewModel
import at.shockbytes.dante.util.addTo
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
class SearchActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)[SearchViewModel::class.java]
        bindViewModel()

        window.exitTransition = Fade()
        window.enterTransition = Fade()

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SearchFragment.newInstance())
            .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private fun bindViewModel() {

        viewModel.bookDownloadEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { item ->
                    supportActionBar?.show()
                    showDownloadFragment(item.isbn)
                }
                .addTo(compositeDisposable)
    }

    private fun showDownloadFragment(query: String) {
        BarcodeScanResultBottomSheetDialogFragment
            .newInstance(query, askForAnotherScan = false, showNotMyBookButton = false)
            .setOnBookAddedListener { bookTitle ->
                showToast(getString(R.string.book_added_to_library, bookTitle))
                supportFinishAfterTransition()
            }
            .show(supportFragmentManager, "bottom-sheet-add-search")
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }
}