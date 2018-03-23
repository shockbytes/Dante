package at.shockbytes.dante.ui.fragment


import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookAdapter
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.OnClick
import com.crashlytics.android.Crashlytics
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotterknife.bindView
import kotterknife.bindViews
import javax.inject.Inject


class DownloadBookFragment : BaseFragment(), Callback,
        Palette.PaletteAsyncListener, BaseAdapter.OnItemClickListener<Book> {

    interface OnBookDownloadedListener {

        fun onBookDownloaded(book: Book)

        fun onCancelDownload()

        fun onErrorDownload(reason: String)

        fun onCloseOnError()

        fun colorSystemBars(actionBarColor: Int?, actionBarTextColor: Int?,
                            statusBarColor: Int?, title: String?)

    }

    private val animViews: List<View> by bindViews(R.id.fragment_download_book_btn_upcoming,
            R.id.fragment_download_book_btn_current,
            R.id.fragment_download_book_btn_done,
            R.id.fragment_download_book_btn_not_my_book)

    private val imgViewCover: ImageView by bindView(R.id.fragment_download_book_imgview_cover)
    private val txtTitle: TextView by bindView(R.id.fragment_download_book_txt_title)
    private val rvOtherSuggestions: RecyclerView by bindView(R.id.fragment_download_book_rv_other_suggestions)
    private val txtOtherSuggestions: TextView by bindView(R.id.fragment_download_book_txt_other_suggestions)
    private val btnNotMyBook: Button by bindView(R.id.fragment_download_book_btn_not_my_book)
    private val progressBar: ProgressBar by bindView(R.id.fragment_download_book_progressbar)
    private val mainView: View by bindView(R.id.fragment_download_book_main_view)
    private val errorView: View by bindView(R.id.fragment_download_book_error_view)
    private val txtErrorCause: TextView by bindView(R.id.fragment_download_book_txt_error_cause)

    @Inject
    protected lateinit var bookManager: BookManager

    private var bookAdapter: BookAdapter? = null

    private var query: String? = null

    private var listener: OnBookDownloadedListener? = null

    private var selectedBook: Book? = null

    private var isOtherSuggestionsShowing: Boolean = false

    override val layoutId = R.layout.fragment_download_book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getString(argBarcode)
        isOtherSuggestionsShowing = false
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun setupViews() { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadBook()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as? OnBookDownloadedListener
    }

    override fun onSuccess() {
        val bm = (imgViewCover.drawable as? BitmapDrawable)?.bitmap
        if (bm != null) {
            Palette.from(bm).generate(this)
        }
    }

    override fun onError() { }

    override fun onGenerated(palette: Palette) {

        val actionBarColor = palette.lightMutedSwatch?.rgb
        val actionBarTextColor = palette.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette.darkMutedSwatch?.rgb

        listener?.colorSystemBars(actionBarColor, actionBarTextColor,
                        statusBarColor, selectedBook?.title)
    }

    override fun onItemClick(t: Book, v: View) {
        val index = bookAdapter?.getLocation(t)
        bookAdapter?.deleteEntity(t)
        bookAdapter?.addEntity(index!!, selectedBook!!)

        selectedBook = t
        setTitleAndIcon(selectedBook)
        rvOtherSuggestions.scrollToPosition(0)
    }


    @OnClick(R.id.fragment_download_book_btn_error_close)
    fun onClickCloseError() {
        listener?.onCloseOnError()
    }

    @OnClick(R.id.fragment_download_book_btn_not_my_book)
    fun onClickNotMyBook() {

        if (!isOtherSuggestionsShowing) {

            txtTitle.animate().translationY(0f).setDuration(500)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        rvOtherSuggestions.animate().alpha(1f).start()
                        txtOtherSuggestions.animate().alpha(1f).start()
                    }.start()
            imgViewCover.animate().translationY(0f).setDuration(500)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()

            btnNotMyBook.setText(R.string.download_suggestions_none)

            isOtherSuggestionsShowing = true
        } else {
            listener?.onCancelDownload()
        }

    }

    @OnClick(R.id.fragment_download_book_btn_upcoming)
    fun onClickUpcoming() {
        finishBookDownload(Book.State.READ_LATER)
    }

    @OnClick(R.id.fragment_download_book_btn_current)
    fun onClickCurrent() {
        finishBookDownload(Book.State.READING)
    }

    @OnClick(R.id.fragment_download_book_btn_done)
    fun onClickDone() {
        finishBookDownload(Book.State.READ)
    }

    private fun finishBookDownload(bookState: Book.State) {

        if (selectedBook != null) {
            // Set the state and store it in database
            selectedBook?.state = bookState
            selectedBook = bookManager.addBook(selectedBook!!) // Return the object with set ID
            listener?.onBookDownloaded(selectedBook!!)
        }
    }

    private fun downloadBook() {

        bookManager.downloadBook(query).subscribe({ suggestion ->
            animateBookViews()
            if (suggestion != null && suggestion.hasSuggestions) {
                selectedBook = suggestion.mainSuggestion
                setTitleAndIcon(selectedBook)
                setupOtherSuggestionsRecyclerView(suggestion.otherSuggestions)
            } else {
                listener?.onErrorDownload("no suggestions")
                showErrorLayout(getString(R.string.download_book_json_error))
            }
        }) { throwable ->
            throwable.printStackTrace()
            showErrorLayout(throwable)
            listener?.onErrorDownload(throwable.localizedMessage)
        }
    }

    private fun animateBookViews() {

        // Hide progressbar smoothly
        progressBar.animate().alpha(0f).scaleY(0.5f).scaleX(0.5f).setDuration(500).start()
        DanteUtils.listPopAnimation(animViews, 250, interpolator = OvershootInterpolator(4f))
    }

    private fun setTitleAndIcon(mainBook: Book?) {

        txtTitle.text = mainBook?.title

        if (!mainBook?.thumbnailAddress.isNullOrEmpty()) {
            Picasso.with(context).load(mainBook?.thumbnailAddress)
                    .placeholder(R.drawable.ic_placeholder).into(imgViewCover, this)
        } else {
            imgViewCover.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setupOtherSuggestionsRecyclerView(books: List<Book>) {

        bookAdapter = BookAdapter(context!!, books, Book.State.READ,
                null, false)
        rvOtherSuggestions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bookAdapter?.onItemClickListener = this
        rvOtherSuggestions.adapter = bookAdapter
    }

    private fun showErrorLayout(error: Throwable) {
        val cause = getString(R.string.download_code_error)
        showErrorLayout(cause)
        Crashlytics.logException(error)
    }

    private fun showErrorLayout(cause: String) {
        if (isAdded) {
            mainView.visibility = View.GONE
            errorView.visibility = View.VISIBLE
            errorView.animate().alpha(1f).start()
            txtErrorCause.text = cause
        } else {
            showToast(cause, true)
            // Log this message, because this should not happen
            Crashlytics.logException(IllegalArgumentException("Cannot show error layout, because DownloadBookFragment is not attached to Activity"))
        }

    }

    companion object {

        private const val argBarcode = "arg_barcode"

        fun newInstance(barcode: String?): DownloadBookFragment {
            val fragment = DownloadBookFragment()
            val args = Bundle()
            args.putString(argBarcode, barcode)
            fragment.arguments = args
            return fragment
        }
    }


}
