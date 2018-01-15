package at.shockbytes.dante.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.ui.fragment.QueryCaptureFragment
import at.shockbytes.dante.util.AppParams
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.tracking.Tracker
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 01.01.2018.
 */

class BookRetrievalActivity: TintableBackNavigableActivity(),
        QueryCaptureFragment.QueryCaptureCallback, DownloadBookFragment.OnBookDownloadedListener {

    @Inject
    protected lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_query_capture)
        // Set this, otherwise this will trigger a Kotlin Exception
        setResult(Activity.RESULT_CANCELED, Intent())

        showQueryFragment()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCameraPermissionDenied() {
        supportFinishAfterTransition()
    }

    override fun onQueryAvailable(query: String?) {
        showDownloadFragment(query)
        setTintableHomeAsUpIndicator(R.drawable.ic_cancel)
    }

    override fun onScannerNotOperational() {
        showToast(R.string.scanner_not_operational)
    }

    override fun onBookDownloaded(book: Book) {
        tracker.trackOnBookScanned(book)
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

    private fun finishBookDownload(bookId: Long, isSuccessful: Boolean) {
        val resultCode = if (isSuccessful) Activity.RESULT_OK else Activity.RESULT_CANCELED
        setResult(resultCode, Intent().putExtra(AppParams.extraBookId, bookId))
        supportFinishAfterTransition()
    }

    private fun showQueryFragment() {
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, QueryCaptureFragment.newInstance())
                .commit()
    }

    private fun showDownloadFragment(query: String?) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(android.R.id.content, DownloadBookFragment.newInstance(query))
                .commit()
    }


    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, BookRetrievalActivity::class.java)
        }

    }
}