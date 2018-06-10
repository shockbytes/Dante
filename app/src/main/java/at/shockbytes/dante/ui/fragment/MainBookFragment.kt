package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookAdapter
import at.shockbytes.dante.books.BookListener
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.books.Book
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import kotterknife.bindView
import javax.inject.Inject


class MainBookFragment : BaseFragment(), BaseAdapter.OnItemClickListener<Book>,
        BookListener, BaseAdapter.OnItemMoveListener<Book> {

    @Inject
    protected lateinit var bookManager: BookManager

    @Inject
    protected lateinit var settings: DanteSettings

    private val recyclerView: RecyclerView by bindView(R.id.fragment_book_main_rv)
    private val emptyView: TextView by bindView(R.id.fragment_book_main_empty_view)

    private lateinit var bookState: Book.State
    private var bookAdapter: BookAdapter? = null

    private var isInitialized: Boolean = false

    private var popupItemSelectedListener: BookAdapter.OnBookPopupItemSelectedListener? = null

    private var selectedItem: Book? = null

    private val layoutManager: RecyclerView.LayoutManager
        get() = if (resources.getBoolean(R.bool.isTablet)) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            else
                StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
        } else {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }


    override val layoutId = R.layout.fragment_book_main

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        popupItemSelectedListener = context as? BookAdapter.OnBookPopupItemSelectedListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookState = arguments?.getSerializable(argState) as Book.State
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
        isInitialized = true

        bookAdapter?.onItemMayChanged(selectedItem)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser and isInitialized) {
            loadBooks()
        }
    }

    override fun setupViews() {

        // Initialize text for empty indicator
        emptyView.text = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]

        // Initialize RecyclerView
        bookAdapter = BookAdapter(context!!, listOf(), bookState,
                popupItemSelectedListener, true, settings)
        recyclerView.layoutManager = layoutManager
        bookAdapter?.onItemClickListener = this
        bookAdapter?.onItemMoveListener = this
        recyclerView.adapter = bookAdapter

        // Setup RecyclerView's ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(BaseItemTouchHelper(bookAdapter!!, // Safe to call, because it is created above
                false, BaseItemTouchHelper.DragAccess.VERTICAL))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onItemClick(t: Book, v: View) {
        selectedItem = t

        context?.let { ctx ->
            startActivity(DetailActivity.newIntent(ctx, t.id, t.title), getTransitionBundle(v))
        }
    }

    override fun onItemDismissed(t: Book, position: Int) {
        // Not supported
    }

    override fun onItemMove(t: Book, from: Int, to: Int) {
        // Do nothing, only react to move actions in the on item move finished method
    }

    override fun onItemMoveFinished() {
        bookManager.updateBookPositions(bookAdapter?.data)
    }

    override fun onBookAdded(book: Book) {
        if (book.state == bookState) {
            bookAdapter?.addEntityAtFirst(book)
            recyclerView.scrollToPosition(0)
            animateEmptyView(false)
        }
    }

    override fun onBookDeleted(book: Book) {
        animateEmptyView(true)
    }

    override fun onBookStateChanged(book: Book, state: Book.State) {
        animateEmptyView(true)
    }

    private fun loadBooks() {
        bookManager.getBooksByState(bookState).subscribe ({ books ->
            if (bookAdapter?.data !== books) {
                bookAdapter?.data = ArrayList(books)
                animateEmptyView(false)
            }
        }, ({ e ->
            e.printStackTrace()
            showSnackbar(getString(R.string.error_load_books))
            Log.e("Dante", "Cannot load books --> " + e.toString())
        }))
    }

    private fun getTransitionBundle(v: View): Bundle? {
        return ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity!!,
                        Pair(v.findViewById(R.id.item_book_card),
                                getString(R.string.transition_name_card)),
                        Pair(v.findViewById(R.id.item_book_img_thumb),
                                getString(R.string.transition_name_thumb)),
                        Pair(v.findViewById(R.id.item_book_txt_title),
                                getString(R.string.transition_name_title)),
                        Pair(v.findViewById(R.id.item_book_txt_subtitle),
                                getString(R.string.transition_name_subtitle)),
                        Pair(v.findViewById(R.id.item_book_txt_author),
                                getString(R.string.transition_name_author))
                ).toBundle()
    }

    private fun animateEmptyView(animate: Boolean) {

        if (animate) {
            emptyView.animate()
                    .alpha((if ((bookAdapter?.itemCount ?: 0) > 0) 0f else 1f))
                    .setDuration(450)
                    .start()
        } else {
            emptyView.alpha = (if ((bookAdapter?.itemCount ?: 0) > 0) 0f else 1f)
        }
    }

    companion object {

        private const val argState = "arg_state"

        fun newInstance(state: Book.State): MainBookFragment {
            val fragment = MainBookFragment()
            val args = Bundle(1)
            args.putSerializable(argState, state)
            fragment.arguments = args
            return fragment
        }
    }
}
