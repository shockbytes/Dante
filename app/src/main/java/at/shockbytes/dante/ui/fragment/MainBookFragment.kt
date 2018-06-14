package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.adapter.BookAdapter
import at.shockbytes.dante.ui.viewmodel.BookListViewModel
import at.shockbytes.dante.util.DanteSettings
import at.shockbytes.dante.util.createSharingIntent
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import kotterknife.bindView
import javax.inject.Inject


class MainBookFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BookEntity>,
        BaseAdapter.OnItemMoveListener<BookEntity>, BookAdapter.OnBookPopupItemSelectedListener {

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    protected lateinit var settings: DanteSettings

    @Inject
    protected lateinit var tracker: Tracker

    private val recyclerView: RecyclerView by bindView(R.id.fragment_book_main_rv)
    private val emptyView: TextView by bindView(R.id.fragment_book_main_empty_view)

    private var bookAdapter: BookAdapter? = null

    private lateinit var viewModel: BookListViewModel

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

    private lateinit var bookState: BookState

    override val layoutId = R.layout.fragment_book_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[BookListViewModel::class.java]

        bookState = arguments?.getSerializable(argState) as BookState
        viewModel.state = bookState
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun setupViews() {

        // Initialize text for empty indicator
        emptyView.text = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]

        // Initialize RecyclerView
        bookAdapter = BookAdapter(context!!, listOf(), bookState, this, true, settings)
        recyclerView.layoutManager = layoutManager
        bookAdapter?.onItemClickListener = this
        bookAdapter?.onItemMoveListener = this
        recyclerView.adapter = bookAdapter

        // Setup RecyclerView's ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(BaseItemTouchHelper(bookAdapter!!, // Safe to call, because it is created above
                false, BaseItemTouchHelper.DragAccess.VERTICAL))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        setupObserver()
    }

    override fun onItemClick(t: BookEntity, v: View) {
        context?.let { ctx ->
            startActivity(DetailActivity.newIntent(ctx, t.id, t.title), getTransitionBundle(v))
        }
    }

    override fun onItemDismissed(t: BookEntity, position: Int) {
        // Not supported
    }

    override fun onItemMove(t: BookEntity, from: Int, to: Int) {
        // Do nothing, only react to move actions in the on item move finished method
    }

    override fun onItemMoveFinished() {
        bookAdapter?.data?.let {
            viewModel.updateBookPositions(it)
        }
    }

    override fun onDelete(b: BookEntity) {
        viewModel.deleteBook(b)
    }

    override fun onShare(b: BookEntity) {
        context?.let { ctx ->
            tracker.trackOnBookShared()
            val sendIntent = b.createSharingIntent(ctx)
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
        }
    }

    override fun onMoveToUpcoming(b: BookEntity) {
        viewModel.moveBookToUpcomingList(b)
    }

    override fun onMoveToCurrent(b: BookEntity) {
        viewModel.moveBookToCurrentList(b)
    }

    override fun onMoveToDone(b: BookEntity) {
        viewModel.moveBookToDoneList(b)
    }

    // --------------------------------------------------------------

    private fun setupObserver() {

        viewModel.books.observe(this, Observer { b ->

            b?.let { books ->
                if (books.isNotEmpty()) {
                    bookAdapter?.updateData(books)
                    updateEmptyView(false)
                    recyclerView.scrollToPosition(0)
                }
            }
        })

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

    private fun updateEmptyView(animate: Boolean) {

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

        fun newInstance(state: BookState): MainBookFragment {
            val fragment = MainBookFragment()
            val args = Bundle(1)
            args.putSerializable(argState, state)
            fragment.arguments = args
            return fragment
        }
    }
}
