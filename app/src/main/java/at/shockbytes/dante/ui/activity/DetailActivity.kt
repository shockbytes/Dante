package at.shockbytes.dante.ui.activity

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.support.v7.widget.PopupMenu
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.dialog.NotesDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.PageEditDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.RateBookDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.tracking.Tracker
import butterknife.OnClick
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotterknife.bindView
import org.joda.time.DateTime
import ru.bullyboo.view.CircleSeekBar
import java.util.*
import javax.inject.Inject

class DetailActivity : TintableBackNavigableActivity(), Callback,
        Palette.PaletteAsyncListener, CircleSeekBar.Callback {

    @Inject
    protected lateinit var manager: BookManager

    @Inject
    protected lateinit var settings: DanteSettings

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

    private lateinit var popupBookCover: PopupMenu
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
            loadImage(book.thumbnailAddress)
        }

        setupNotes()
        setupPageComponents()
        setupBookCoverChange()
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

    override fun onStartScrolling(startValue: Int) {}

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

    private fun setupPageComponents() {
        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (settings.pageTrackingEnabled && book.reading && book.hasPages) {
            sbPages.maxValue = book.pageCount
            sbPages.value = book.currentPage
            sbPages.setCallback(this)
            sbPages.setOnValueChangedListener { page ->
                btnPages.text = getString(R.string.detail_pages, page, book.pageCount)
            }

            // Show pages as button text
            val pages = if (book.state == Book.State.READING)
                getString(R.string.detail_pages, book.currentPage, book.pageCount)
            else
                book.pageCount.toString()
            btnPages.text = pages

        } else {
            sbPages.visibility = View.GONE
            // Show all pages, but disable button clicking
            btnPages.text = book.pageCount.toString()
        }
    }

    private fun startComponentAnimations() {
        val duration = if (DanteUtils.isPortrait(this)) 150L else 300L
        DanteUtils.listPopAnimation(animationList, duration, 500, DecelerateInterpolator(2f))
    }

    private fun setupBookCoverChange() {
    /* TODO Enable in V3.0
    popupBookCover = PopupMenu(this, imgViewThumb)
    popupBookCover.menuInflater.inflate(R.menu.popup_item_book_cover, popupBookCover.menu)
    popupBookCover.setOnMenuItemClickListener {

        val source = DanteUtils.getImagePickerSourceByItemId(it.itemId)
        RxImagePicker.with(this).requestImage(source)
                .bindToLifecycle(this)
                .subscribe {
                    manager.updateBookCover(book, it.toString())
                    Log.wtf("Dante", it.toString())
                    loadImage(it.toString())
                }
        true
    }
    DanteUtils.tryShowIconsInPopupMenu(popupBookCover)
    */
    }

    private fun setupNotes() {
        val notesId = if (!book.notes.isNullOrEmpty()) R.string.my_notes else R.string.add_notes
        btnNotes.text = getString(notesId)
    }

    private fun loadImage(address: String?) {
        Picasso.with(applicationContext)
                .load(address)
                .placeholder(R.drawable.ic_placeholder)
                .into(imgViewThumb, this)
    }

    private fun onUpdatePublishedDate(y: String, m: String, d: String) {

        val mStr = m.padStart(2, '0')
        val dStr = d.padStart(2, '0')
        val publishedDate = "$y-$mStr-$dStr"

        btnPublished.text = publishedDate
        manager.updateBookPublishedDate(book, publishedDate)
    }

    @OnClick(R.id.activity_detail_btn_rating)
    protected fun onClickRateBook(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        RateBookDialogFragment.newInstance(book.title, book.thumbnailAddress, book.rating)
                .setOnApplyListener { rating ->
                    manager.updateBookRating(book, rating)
                    tracker.trackRatingEvent(rating)
                    btnRating.text = resources.getQuantityString(R.plurals.book_rating, rating, rating)
                }.show(supportFragmentManager, "rating-dialogfragment")
    }

    @OnClick(R.id.activity_detail_btn_pages)
    protected fun onClickPages(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        // Only show current page in dialog if tracking is enabled and book is in reading state
        val showCurrentPage = settings.pageTrackingEnabled and book.reading
        PageEditDialogFragment.newInstance(book.currentPage, book.pageCount, showCurrentPage)
                .setOnPageEditedListener { current, pages ->
                    manager.updateBookPages(book, current, pages)
                    setupPageComponents()
                }.show(supportFragmentManager, "pages-dialogfragment")
    }

    @OnClick(R.id.activity_detail_btn_notes)
    protected fun onClickNotes(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        NotesDialogFragment.newInstance(book.title, book.thumbnailAddress, book.notes ?: "")
                .setOnApplyListener { notes ->
                    manager.updateBookNotes(book, notes)
                    setupNotes()
                }.show(supportFragmentManager, "notes-dialogfragment")
    }

    // TODO Enable in v3.0
    /*
    @OnClick(R.id.activity_detail_img_thumb)
    protected fun onClickBookCover(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        popupBookCover.show()
    }
    */

    @OnClick(R.id.activity_detail_btn_published)
    protected fun onClickPublishedDate(v: View) {
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        val cal = Calendar.getInstance()
        DatePickerDialog(this,
                { _, y, m, d -> onUpdatePublishedDate(y.toString(), m.plus(1).toString(), d.toString()) }, // +1 because month starts with 0
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    companion object {

        private const val ARG_ID = "arg_id"

        fun newIntent(context: Context, id: Long): Intent {
            return Intent(context, DetailActivity::class.java).putExtra(ARG_ID, id)
        }
    }

}
