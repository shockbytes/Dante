package at.shockbytes.dante.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.fragment.DownloadBookFragment
import at.shockbytes.dante.util.AppParams
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.tracking.Tracker
import javax.inject.Inject

class DownloadActivity : BackNavigableActivity(), DownloadBookFragment.OnBookDownloadedListener {

    @Inject
    protected lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        setResult(Activity.RESULT_CANCELED, Intent()) // To avoid Kotlin exception
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cancel)

        val query = intent.extras?.getString(ARG_QUERY)
        showDownloadFragment(query)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onBookDownloaded(book: Book) {
        tracker.trackOnBookScanned(book)
        forwardToCaller(book.id, true)
    }

    override fun onCancelDownload() {
        tracker.trackOnFoundBookCanceled()
        forwardToCaller(-1, false)
    }

    override fun onErrorDownload(reason: String) {
        tracker.trackOnDownloadError(reason)
    }

    override fun onCloseOnError() {
        forwardToCaller(-1, false)
    }

    fun colorSystemBars(actionBarColor: Int?, actionBarTextColor: Int?,
                        statusBarColor: Int?, bookTitle: String?) {

        // Default initialize if not set
        val abColor = actionBarColor ?: ContextCompat.getColor(this, R.color.colorPrimary)
        val abtColor = actionBarTextColor ?: ContextCompat.getColor(this, android.R.color.white)
        val sbColor = statusBarColor ?: ContextCompat.getColor(this, R.color.colorPrimaryDark)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(abColor))
        val text = SpannableString(bookTitle)
        text.setSpan(ForegroundColorSpan(abtColor), 0, text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        supportActionBar?.title = text

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = sbColor
        }
    }

    private fun forwardToCaller(bookId: Long, isSuccessful: Boolean) {
        val resultCode = if (isSuccessful) Activity.RESULT_OK else Activity.RESULT_CANCELED
        setResult(resultCode, Intent().putExtra(AppParams.EXTRA_BOOK_ID, bookId))
        supportFinishAfterTransition()
    }

    private fun showDownloadFragment(query: String?) {

        val fragment = DownloadBookFragment.newInstance(query)
                .setOnBookDownloadedListener(this)
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.activity_download_main_content, fragment)
                .commit()
    }

    companion object {

        private val ARG_QUERY = "arg_barcode"

        fun newIntent(context: Context, query: String): Intent {
            return Intent(context, DownloadActivity::class.java).putExtra(ARG_QUERY, query)
        }
    }

}
