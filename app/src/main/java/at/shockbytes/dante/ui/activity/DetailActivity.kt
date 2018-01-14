package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.CardView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialogs.SimpleRequestDialogFragment
import at.shockbytes.dante.util.books.Book
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotterknife.bindView
import org.joda.time.DateTime
import javax.inject.Inject

class DetailActivity : TintableBackNavigableActivity(), Callback,
        Palette.PaletteAsyncListener, SeekBar.OnSeekBarChangeListener {

    @Inject
    protected lateinit var manager: BookManager

    @Inject
    protected lateinit var prefs: SharedPreferences

    private val imgViewThumb: ImageView by bindView(R.id.activity_detail_img_thumb)
    private val txtTitle: TextView by bindView(R.id.activity_detail_txt_title)
    private val txtSubTitle: TextView by bindView(R.id.activity_detail_txt_subtitle)
    private val txtAuthor: TextView by bindView(R.id.activity_detail_txt_author)
    private val txtPages: TextView by bindView(R.id.activity_detail_txt_pages)
    private val txtPublished: TextView by bindView(R.id.activity_detail_txt_published)
    private val txtIsbn: TextView by bindView(R.id.activity_detail_txt_isbn)
    private val cardViewDates: CardView by bindView(R.id.activity_detail_cardview_dates)
    private val txtWishlistdate: TextView by bindView(R.id.activity_detail_txt_wishlist_date)
    private val txtStartdate: TextView by bindView(R.id.activity_detail_txt_start_date)
    private val txtEnddate: TextView by bindView(R.id.activity_detail_txt_end_date)
    private val seekBarPages: AppCompatSeekBar by bindView(R.id.activity_detail_seekbar_pages)

    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val id = intent.getLongExtra(ARG_ID, -1)
        if (id > -1L) {
            book = manager.getBook(id)
            initialize()
        } else {
            Toast.makeText(applicationContext, R.string.error_load_book, Toast.LENGTH_LONG).show()
            supportFinishAfterTransition()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private fun initialize() {

        supportActionBar?.title = book.title

        initializeMainCard()
        initializeTimeCard()
    }

    private fun initializeMainCard() {

        txtTitle.text = book.title
        txtAuthor.text = book.author

        txtPublished.text = if (!book.publishedDate.isEmpty()) book.publishedDate else "---"
        txtIsbn.text = if (!book.isbn.isEmpty()) book.isbn else "---"

        // Hide subtitle if not available
        val subtitle = book.subTitle
        if (subtitle.isEmpty()) {
            txtSubTitle.visibility = View.GONE
        } else {
            txtSubTitle.text = subtitle
        }

        val thumbnailAddress = book.thumbnailAddress
        if (thumbnailAddress?.isEmpty() == false) {
            Picasso.with(applicationContext)
                    .load(thumbnailAddress)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imgViewThumb, this)
        }

        // Setup pages and SeekBar
        val pages = if (book.state == Book.State.READING)
            getString(R.string.detail_pages, book.currentPage, book.pageCount)
        else
            book.pageCount.toString()

        txtPages.text = pages

        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (prefs.getBoolean(getString(R.string.prefs_page_tracking_key), true)
                && book.state == Book.State.READING
                && book.pageCount > 0) {
            seekBarPages.max = book.pageCount
            seekBarPages.progress = book.currentPage
            seekBarPages.setOnSeekBarChangeListener(this)
        } else {
            seekBarPages.visibility = View.GONE
        }
    }

    private fun initializeTimeCard() {

        // Hide complete card if no time information is available
        if (!book.isAnyTimeInformationAvailable) {
            cardViewDates.visibility = View.GONE
        } else {

            val pattern = "dd. MMM yyyy"
            // Check if wishlist date is available
            if (book.wishlistDate > 0) {
                val wishlistDate = DateTime(book.wishlistDate).toString(pattern)
                txtWishlistdate.text = getString(R.string.detail_wishlist_date, wishlistDate)
            } else {
                txtWishlistdate.visibility = View.GONE
            }

            // Check if start date is available
            if (book.startDate > 0) {
                val startDate = DateTime(book.startDate).toString(pattern)
                txtStartdate.text = getString(R.string.detail_start_date, startDate)
            } else {
                txtStartdate.visibility = View.GONE
            }

            // Check if end date is available
            if (book.endDate > 0) {
                val endDate = DateTime(book.endDate).toString(pattern)
                txtEnddate.text = getString(R.string.detail_end_date, endDate)
            } else {
                txtEnddate.visibility = View.GONE
            }
        }
    }

    override fun onSuccess() {
        val bm = (imgViewThumb.drawable as? BitmapDrawable)?.bitmap
        if (bm != null) {
            Palette.from(bm).generate(this)
        }
    }

    override fun onError() {}

    override fun onGenerated(palette: Palette) {

        val actionBarColor = palette.lightMutedSwatch?.rgb
        val actionBarTextColor = palette.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette.darkMutedSwatch?.rgb
        tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor, book.title)
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        txtPages.text = getString(R.string.detail_pages, i, book.pageCount)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {

        manager.updateCurrentBookPage(book, seekBar.progress)

        if (book.currentPage == book.pageCount) {
            SimpleRequestDialogFragment.newInstance(getString(R.string.book_finished, book.title),
                    getString(R.string.book_finished_move_to_done_question), R.drawable.ic_pick_done)
                    .setOnAcceptListener {
                        manager.updateBookState(book, Book.State.READ)
                        supportFinishAfterTransition()
                    }
                    .show(supportFragmentManager, "book-finished-dialogfragment")
        }
    }

    companion object {

        private val ARG_ID = "arg_id"

        fun newIntent(context: Context, id: Long): Intent {
            return Intent(context, DetailActivity::class.java).putExtra(ARG_ID, id)
        }
    }

}
