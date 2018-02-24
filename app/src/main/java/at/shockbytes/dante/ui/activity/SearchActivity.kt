package at.shockbytes.dante.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Fade
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerTintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.ui.fragment.SearchFragment
import at.shockbytes.dante.books.BookSearchSuggestion
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.tracking.Tracker
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 03.02.2018.
 */

class SearchActivity : ContainerTintableBackNavigableActivity(),
        SearchFragment.OnBookSuggestionDownloadClickListener,
        DownloadBookFragment.OnBookDownloadedListener {

    @Inject
    protected lateinit var tracker: Tracker

    override val displayFragment: Fragment
        get() = SearchFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.exitTransition = Fade()
            window.enterTransition = Fade()
        }
        supportActionBar?.setShowHideAnimationEnabled(true)
        supportActionBar?.hide()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onBookSuggestionClicked(s: BookSearchSuggestion) {
        supportActionBar?.show()
        showDownloadFragment(s.isbn)
    }

    override fun onBookDownloaded(book: Book) {
        tracker.trackOnBookScanned(book, true)
        finishBookDownload(book.id, true)
    }

    override fun onCancelDownload() {
        tracker.trackOnFoundBookCanceled()
        finishBookDownload(-1, false)
    }

    override fun onErrorDownload(reason: String) {
        tracker.trackOnDownloadError(reason)
    }

    override fun onCloseOnError() {
        finishBookDownload(-1, false)
    }

    override fun colorSystemBars(actionBarColor: Int?, actionBarTextColor: Int?,
                                 statusBarColor: Int?, title: String?) {
        tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor, title, true)
    }

    override fun onBackStackPopped() {
        super.onBackStackPopped()
        supportActionBar?.hide()
        // Use default colors and default title
        tintSystemBarsWithText(null, null, null,
                getString(R.string.search), true)

    }

    private fun finishBookDownload(bookId: Long, isSuccessful: Boolean) {
        val resultCode = if (isSuccessful) Activity.RESULT_OK else Activity.RESULT_CANCELED
        setResult(resultCode, Intent().putExtra(DanteUtils.extraBookId, bookId))
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