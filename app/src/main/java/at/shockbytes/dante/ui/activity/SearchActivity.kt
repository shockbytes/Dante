package at.shockbytes.dante.ui.activity

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.activity.core.ContainerTintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.ui.fragment.SearchFragment
import at.shockbytes.dante.ui.viewmodel.SearchViewModel
import at.shockbytes.dante.util.addTo
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
class SearchActivity : ContainerTintableBackNavigableActivity(), DownloadBookFragment.OnBookDownloadedListener {

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: SearchViewModel

    override val displayFragment: androidx.fragment.app.Fragment
        get() = SearchFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)[SearchViewModel::class.java]
        bindViewModel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.exitTransition = Fade()
            window.enterTransition = Fade()
        }

        supportActionBar?.let { actionBar ->
            actionBar.title = ""
            actionBar.setShowHideAnimationEnabled(true)
            actionBar.hide()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onBookDownloaded(book: BookEntity) {
        finishBookDownload()
    }

    override fun onCancelDownload() {
        finishBookDownload()
    }

    override fun onErrorDownload(reason: String, isAttached: Boolean) {

        if (!isAttached) {
            showToast(R.string.download_attachment_error)
            supportFinishAfterTransition()
        }
    }

    override fun onCloseOnError() {
        finishBookDownload()
    }

    override fun colorSystemBars(
        actionBarColor: Int?,
        actionBarTextColor: Int?,
        statusBarColor: Int?,
        title: String?
    ) {
        tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor, title, true)
    }

    override fun onBackStackPopped() {
        super.onBackStackPopped()
        supportActionBar?.hide()
        // Use default colors and default title
        tintSystemBarsWithText(null, null, null, "", true)
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

    private fun finishBookDownload() {
        supportFinishAfterTransition()
    }

    private fun showDownloadFragment(query: String?) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .add(android.R.id.content, DownloadBookFragment.newInstance(query))
                .addToBackStack(null)
                .commit()
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }
}