package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookAdapter
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.util.books.Book
import at.shockbytes.dante.util.books.BookListener
import at.shockbytes.dante.util.books.BookManager
import at.shockbytes.util.adapter.BaseAdapter
import kotterknife.bindView
import javax.inject.Inject

/**
 * A placeholder fragment containing a simple view.
 */
class MainBookFragment : BaseFragment(), BaseAdapter.OnItemClickListener<Book>, BookListener {

    private val recyclerView: RecyclerView by bindView(R.id.fragment_book_main_rv)

    private val emptyView: TextView by bindView(R.id.fragment_book_main_empty_view)

    @Inject
    protected lateinit var bookManager: BookManager

    private lateinit var bookState: Book.State
    private var bookAdapter: BookAdapter? = null

    private var popupItemSelectedListener: BookAdapter.OnBookPopupItemSelectedListener? = null

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


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        popupItemSelectedListener = context as? BookAdapter.OnBookPopupItemSelectedListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookState = arguments.getSerializable(ARG_STATE) as Book.State
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_book_main, container, false)
    }

    override fun onResume() {
        super.onResume()

        bookManager.getBooksByState(bookState).subscribe ({ books ->
            bookAdapter?.data = books.toMutableList()
            animateEmptyView(false)
        }, ({ e ->
            showSnackbar(getString(R.string.error_load_books))
            Log.e("Dante", "Cannot load books --> " + e.toString())
        }))

    }

    public override fun setupViews() {

        // Initialize text for empty indicator
        val empty = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]
        emptyView.text = empty

        // Initialize RecyclerView
        bookAdapter = BookAdapter(context, listOf(), bookState,
                popupItemSelectedListener, true)
        recyclerView.layoutManager = layoutManager
        bookAdapter?.onItemClickListener = this
        recyclerView.adapter = bookAdapter
    }

    override fun onItemClick(t: Book, v: View) {
        startActivity(DetailActivity.newIntent(context, t.id), getTransitionBundle(v))
    }

    private fun getTransitionBundle(v: View): Bundle {
        return ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity,
                        Pair(v.findViewById(R.id.listitem_book_card),
                                getString(R.string.transition_name_card)),
                        Pair(v.findViewById(R.id.listitem_book_img_thumb),
                                getString(R.string.transition_name_thumb)),
                        Pair(v.findViewById(R.id.listitem_book_txt_title),
                                getString(R.string.transition_name_title)),
                        Pair(v.findViewById(R.id.listitem_book_txt_subtitle),
                                getString(R.string.transition_name_subtitle)),
                        Pair(v.findViewById(R.id.listitem_book_txt_author),
                                getString(R.string.transition_name_author))
                ).toBundle()
    }

    override fun onBookAdded(book: Book) {
        if (book.state == bookState) {
            bookAdapter?.addEntityAtFirst(book)
            recyclerView.scrollToPosition(0)
            emptyView.alpha = (if ((bookAdapter?.itemCount ?: 0) > 0) 0 else 1).toFloat()
        }
    }

    override fun onBookDeleted(book: Book) {
        animateEmptyView(true)
    }

    override fun onBookStateChanged(book: Book, state: Book.State) {
        animateEmptyView(true)
    }

    private fun animateEmptyView(animate: Boolean) {

        if (animate) {
            emptyView.animate()
                    .alpha((if ((bookAdapter?.itemCount ?: 0) > 0) 0 else 1).toFloat())
                    .setDuration(450)
                    .start()
        } else {
            emptyView.alpha = (if ((bookAdapter?.itemCount ?: 0) > 0) 0 else 1).toFloat()
        }
    }

    companion object {

        private val ARG_STATE = "arg_state"

        fun newInstance(state: Book.State): MainBookFragment {
            val fragment = MainBookFragment()
            val args = Bundle(1)
            args.putSerializable(ARG_STATE, state)
            fragment.arguments = args
            return fragment
        }
    }
}
