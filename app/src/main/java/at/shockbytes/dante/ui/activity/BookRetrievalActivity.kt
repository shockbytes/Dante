package at.shockbytes.dante.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.ui.fragment.QueryCaptureFragment
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

        tracker.trackOnScanBook()

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
        tintHomeAsUpIndicator(R.drawable.ic_cancel)
    }

    override fun onScannerNotOperational() {
        showToast(R.string.scanner_not_operational)
    }

    override fun onBookDownloaded(book: BookEntity) {
        tracker.trackOnBookScanned(book)
        finishBookDownload()
    }

    override fun onCancelDownload() {
        tracker.trackOnFoundBookCanceled()
        finishBookDownload()
    }

    override fun onErrorDownload(reason: String, isAttached: Boolean) {
        tracker.trackOnDownloadError(reason)

        if (!isAttached) {
            showToast(R.string.download_attachment_error)
            supportFinishAfterTransition()
        }
    }

    override fun onCloseOnError() {
        finishBookDownload()
    }

    override fun colorSystemBars(actionBarColor: Int?, actionBarTextColor: Int?,
                                 statusBarColor: Int?, title: String?) {
        tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor, title, true)
    }

    private fun finishBookDownload() {
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