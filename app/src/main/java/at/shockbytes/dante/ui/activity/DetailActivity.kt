package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialogs.RateBookDialogFragment
import at.shockbytes.dante.ui.fragment.dialogs.SimpleRequestDialogFragment
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.tracking.Tracker
import butterknife.OnClick
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotterknife.bindView
import org.joda.time.DateTime
import ru.bullyboo.view.CircleSeekBar
import javax.inject.Inject

class DetailActivity : TintableBackNavigableActivity(), Callback,
        Palette.PaletteAsyncListener, CircleSeekBar.Callback {

    @Inject
    protected lateinit var manager: BookManager

    @Inject
    protected lateinit var prefs: SharedPreferences

    @Inject
    protected lateinit var tracker: Tracker

    private val imgViewThumb: ImageView by bindView(R.id.activity_detail_img_thumb)
    private val txtTitle: TextView by bindView(R.id.activity_detail_txt_title)
    private val txtSubTitle: TextView by bindView(R.id.activity_detail_txt_subtitle)
    private val txtAuthor: TextView by bindView(R.id.activity_detail_txt_author)

    private val btnPublished: Button by bindView(R.id.activity_detail_btn_published)
    private val btnRating: Button by bindView(R.id.activity_detail_btn_rating)
    private val btnNotes: Button by bindView(R.id.activity_detail_btn_notes)
    private val btnPages: Button by bindView(R.id.activity_detail_btn_pages)
    private val sbPages: CircleSeekBar by bindView(R.id.activity_detail_circle_seekbar_pages)

    private val cardViewDates: CardView by bindView(R.id.activity_detail_cardview_dates)
    private val txtWishlistDate: TextView by bindView(R.id.activity_detail_txt_wishlist_date)
    private val txtStartDate: TextView by bindView(R.id.activity_detail_txt_start_date)
    private val txtEndDate: TextView by bindView(R.id.activity_detail_txt_end_date)


    private val animationList: List<View> by lazy {
        listOf(btnPublished, btnRating, sbPages, btnPages, btnNotes)
    }

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

        btnPublished.text = if (!book.publishedDate.isEmpty()) book.publishedDate else "---"
        btnRating.text = if (book.rating in 1..5) {
            resources.getQuantityString(R.plurals.book_rating, book.rating, book.rating)
        } else getString(R.string.rate_book)

        // Hide subtitle if not available
        val subtitle = book.subTitle
        if (subtitle.isEmpty()) {
            txtSubTitle.visibility = View.GONE
        } else {
            txtSubTitle.text = subtitle
        }

        if (!book.thumbnailAddress.isNullOrEmpty()) {
            Picasso.with(applicationContext)
                    .load(book.thumbnailAddress)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(imgViewThumb, this)
        }

        // Setup pages and SeekBar
        val pages = if (book.state == Book.State.READING)
            getString(R.string.detail_pages, book.currentPage, book.pageCount)
        else
            book.pageCount.toString()

        btnPages.text = pages

        setupSeekBar()
        startComponentAnimations()
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
                txtWishlistDate.text = getString(R.string.detail_wishlist_date, wishlistDate)
            } else {
                txtWishlistDate.visibility = View.GONE
            }

            // Check if start date is available
            if (book.startDate > 0) {
                val startDate = DateTime(book.startDate).toString(pattern)
                txtStartDate.text = getString(R.string.detail_start_date, startDate)
            } else {
                txtStartDate.visibility = View.GONE
            }

            // Check if end date is available
            if (book.endDate > 0) {
                val endDate = DateTime(book.endDate).toString(pattern)
                txtEndDate.text = getString(R.string.detail_end_date, endDate)
            } else {
                txtEndDate.visibility = View.GONE
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

    override fun onStartScrolling(startValue: Int) { }

    override fun onEndScrolling(endValue: Int) {

        manager.updateCurrentBookPage(book, endValue)
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

    override fun backwardAnimation() {
        super.backwardAnimation()
        animationList.forEach { it.alpha = 0f }
    }

    private fun setupSeekBar() {
        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (prefs.getBoolean(getString(R.string.prefs_page_tracking_key), true)
                && book.state == Book.State.READING
                && book.pageCount > 0) {
            sbPages.maxValue = book.pageCount
            sbPages.value = book.currentPage
            sbPages.setCallback(this)
            sbPages.setOnValueChangedListener { page ->
                btnPages.text = getString(R.string.detail_pages, page, book.pageCount)
            }
        } else {
            sbPages.visibility = View.GONE
        }
    }

    private fun startComponentAnimations() {
        val duration = if (DanteUtils.isPortrait(this)) 350L else 500L
        DanteUtils.listPopAnimation(animationList, duration, AccelerateDecelerateInterpolator())
    }

    @OnClick(R.id.activity_detail_btn_rating)
    protected fun onClickRateBook(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        RateBookDialogFragment.newInstance(book.title, book.thumbnailAddress)
                .setRatingListener {
                    manager.updateBookRating(book, it)
                    tracker.trackRatingEvent(it)
                    btnRating.text = resources.getQuantityString(R.plurals.book_rating, it, it)
                }.show(supportFragmentManager, "rating-dialogfragment")
    }

    @OnClick(R.id.activity_detail_btn_pages)
    protected fun onClickPages(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    @OnClick(R.id.activity_detail_btn_notes)
    protected fun onClickNotes(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    companion object {

        private val ARG_ID = "arg_id"

        fun newIntent(context: Context, id: Long): Intent {
            return Intent(context, DetailActivity::class.java).putExtra(ARG_ID, id)
        }
    }

}
