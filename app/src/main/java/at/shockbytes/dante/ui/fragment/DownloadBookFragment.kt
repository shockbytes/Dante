package at.shockbytes.dante.ui.fragment


import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookAdapter
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.util.adapter.BaseAdapter
import com.crashlytics.android.Crashlytics
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_download_book.*
import javax.inject.Inject


class DownloadBookFragment : BaseFragment(), Callback,
        Palette.PaletteAsyncListener, BaseAdapter.OnItemClickListener<Book> {

    interface OnBookDownloadedListener {

        fun onBookDownloaded(book: Book)

        fun onCancelDownload()

        fun onErrorDownload(reason: String, isAttached: Boolean)

        fun onCloseOnError()

        fun colorSystemBars(actionBarColor: Int?, actionBarTextColor: Int?,
                            statusBarColor: Int?, title: String?)

    }

    private val drawableResList: List<Pair<Int, TextView>> by lazy {
        listOf(Pair(R.drawable.ic_pick_upcoming, btnDownloadFragmentUpcoming),
                Pair(R.drawable.ic_pick_current, btnDownloadFragmentCurrent),
                Pair(R.drawable.ic_pick_done, btnDownloadFragmentDone))
    }

    private val animViews: List<View> by lazy {
        listOf(btnDownloadFragmentUpcoming,
                btnDownloadFragmentCurrent,
                btnDownloadFragmentDone,
                btnDownloadFragmentNotMyBook)
    }

    @Inject
    protected lateinit var bookManager: BookManager

    private var bookAdapter: BookAdapter? = null
    private var listener: OnBookDownloadedListener? = null
    private var selectedBook: Book? = null
    private var isOtherSuggestionsShowing: Boolean = false
    private var query: String? = null

    override val layoutId = R.layout.fragment_download_book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getString(argBarcode)
        isOtherSuggestionsShowing = false
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun setupViews() {
        loadIcons()
        downloadBook()

        btnDownloadFragmentErrorClose.setOnClickListener {
            listener?.onCloseOnError()
        }
        btnDownloadFragmentNotMyBook.setOnClickListener {
            showOtherSuggestions()
        }
        btnDownloadFragmentUpcoming.setOnClickListener {
            finishBookDownload(Book.State.READ_LATER)
        }
        btnDownloadFragmentCurrent.setOnClickListener {
            finishBookDownload(Book.State.READING)
        }
        btnDownloadFragmentDone.setOnClickListener {
            finishBookDownload(Book.State.READ)
        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as? OnBookDownloadedListener
    }

    override fun onSuccess() {
        (imgViewDownloadFragmentCover.drawable as? BitmapDrawable)?.bitmap?.let {
            Palette.from(it).generate(this)
        }
    }

    override fun onError() {}

    override fun onGenerated(palette: Palette?) {

        val actionBarColor = palette?.lightMutedSwatch?.rgb
        val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
        val statusBarColor = palette?.darkMutedSwatch?.rgb

        listener?.colorSystemBars(actionBarColor, actionBarTextColor,
                statusBarColor, selectedBook?.title)
    }

    override fun onItemClick(t: Book, v: View) {
        val index = bookAdapter?.getLocation(t) ?: return
        bookAdapter?.deleteEntity(t)
        bookAdapter?.addEntity(index, selectedBook!!)

        selectedBook = t
        setTitleAndIcon(selectedBook)
        recyclerViewDownloadFragmentOtherSuggestions.scrollToPosition(0)
    }

    // --------------------------------------------------------------------

    private fun showOtherSuggestions() {

        if (!isOtherSuggestionsShowing) {

            txtDownloadFragmentTitle.animate().translationY(0f).setDuration(300)
                    .setInterpolator(AnticipateInterpolator())
                    .withEndAction {
                        recyclerViewDownloadFragmentOtherSuggestions.animate().alpha(1f).start()
                        txtDownloadFragmentOtherSuggestions.animate().alpha(1f).start()
                    }.start()
            imgViewDownloadFragmentCover.animate().translationY(0f).setDuration(300)
                    .setInterpolator(AnticipateInterpolator())
                    .start()

            btnDownloadFragmentNotMyBook.setText(R.string.download_suggestions_none)

            isOtherSuggestionsShowing = true
        } else {
            listener?.onCancelDownload()
        }
    }

    private fun finishBookDownload(bookState: Book.State) {
        selectedBook?.let { book ->
            // Set the state and store it in database
            book.state = bookState
            selectedBook = bookManager.addBook(book) // Return the object with set ID
            listener?.onBookDownloaded(book)
        }
    }

    private fun downloadBook() {
        bookManager.downloadBook(query).subscribe({ suggestion ->
            animateBookViews()
            if (suggestion != null && suggestion.hasSuggestions) {
                selectedBook = suggestion.mainSuggestion
                setTitleAndIcon(selectedBook)
                setupOtherSuggestionsRecyclerView(suggestion.otherSuggestions)
                activity?.actionBar?.title = selectedBook?.title
            } else {
                listener?.onErrorDownload("no suggestions", isAdded)
                showErrorLayout(getString(R.string.download_book_json_error))
            }
        }) { throwable ->
            throwable.printStackTrace()
            showErrorLayout(throwable)
            listener?.onErrorDownload(throwable.localizedMessage, isAdded)
        }
    }

    private fun loadIcons() {
        Single.fromCallable {
            drawableResList.mapNotNull { (drawableRes, view) ->
                context?.let { ctx ->
                    val drawable = DanteUtils.vector2Drawable(ctx, drawableRes)
                    Pair(drawable, view)
                }
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe { list: List<Pair<Drawable, TextView>> ->
                    list.forEach { (drawable, view) ->
                        view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                    }
                }
    }

    private fun animateBookViews() {
        // Hide progressbar smoothly
        progressBarDownloadFragment.animate()
                .alpha(0f)
                .scaleY(0.5f)
                .scaleX(0.5f)
                .setDuration(500)
                .start()
        DanteUtils.listPopAnimation(animViews, 250, interpolator = OvershootInterpolator(4f))
    }

    private fun setTitleAndIcon(mainBook: Book?) {

        txtDownloadFragmentTitle.text = mainBook?.title

        if (!mainBook?.thumbnailAddress.isNullOrEmpty()) {
            Picasso.with(context).load(mainBook?.thumbnailAddress)
                    .placeholder(DanteUtils.vector2Drawable(context!!, R.drawable.ic_placeholder))
                    .into(imgViewDownloadFragmentCover, this)
        } else {
            imgViewDownloadFragmentCover.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setupOtherSuggestionsRecyclerView(books: List<Book>) {

        bookAdapter = BookAdapter(context!!, books, Book.State.READ,
                null, false)
        recyclerViewDownloadFragmentOtherSuggestions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bookAdapter?.onItemClickListener = this
        recyclerViewDownloadFragmentOtherSuggestions.adapter = bookAdapter
    }

    private fun showErrorLayout(error: Throwable) {
        Crashlytics.logException(error)
        if (isAdded) {
            val cause = getString(R.string.download_code_error)
            showErrorLayout(cause)
        }
    }

    private fun showErrorLayout(cause: String) {
        if (isAdded) {
            layoutDownloadFragmentMain.visibility = View.GONE
            layoutDownloadFragmentError.visibility = View.VISIBLE
            layoutDownloadFragmentError.animate().alpha(1f).start()
            txtDownloadFragmentErrorCause.text = cause
        } else {
            showToast(cause, true)
            // Log this message, because this should not happen
            Crashlytics.logException(IllegalArgumentException("Cannot show error layout, because DownloadBookFragment is not attached to Activity"))
        }

    }

    // --------------------------------------------------------------------

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
