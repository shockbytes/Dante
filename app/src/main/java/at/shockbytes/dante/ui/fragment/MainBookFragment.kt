package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookEntity
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.ActivityNavigation
import at.shockbytes.dante.ui.adapter.BookAdapter
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.viewmodel.BookListViewModel
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import kotlinx.android.synthetic.main.fragment_book_main.*
import javax.inject.Inject

class MainBookFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BookEntity>,
        BaseAdapter.OnItemMoveListener<BookEntity>, BookAdapter.OnBookPopupItemSelectedListener {

    override val layoutId = R.layout.fragment_book_main

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var tracker: Tracker

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var bookState: BookState
    private lateinit var bookAdapter: BookAdapter
    private lateinit var viewModel: BookListViewModel

    private val rvLayoutManager: RecyclerView.LayoutManager
        get() = if (resources.getBoolean(R.bool.isTablet)) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            else
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[BookListViewModel::class.java]

        bookState = arguments?.getSerializable(ARG_STATE) as BookState
        viewModel.state = bookState
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        fragment_book_main_rv.isLayoutFrozen = false
    }

    override fun onPause() {
        super.onPause()
        fragment_book_main_rv.isLayoutFrozen = true
    }

    override fun bindViewModel() {
        viewModel.getBooks().observe(this, Observer { state ->

            when (state) {
                is BookListViewModel.BookLoadingState.Success -> {
                    updateEmptyView(hide = true, animate = false)
                    bookAdapter.updateData(state.books)
                    fragment_book_main_rv.smoothScrollToPosition(0)
                }

                is BookListViewModel.BookLoadingState.Empty -> {
                    updateEmptyView(hide = false, animate = true)
                }

                is BookListViewModel.BookLoadingState.Error -> {
                    showSnackbar(getString(R.string.load_error), showLong = true)
                }
            }
        })
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {

        // Initialize text for empty indicator
        fragment_book_main_empty_view.text = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]

        // Initialize RecyclerView
        context?.let { ctx ->
            bookAdapter = BookAdapter(ctx, bookState, imageLoader, this, true).apply {
                onItemClickListener = this@MainBookFragment
                onItemMoveListener = this@MainBookFragment
            }
            fragment_book_main_rv.apply {
                layoutManager = rvLayoutManager
                adapter = bookAdapter
            }
        }

        // Setup RecyclerView's ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(BaseItemTouchHelper(bookAdapter,
                false, BaseItemTouchHelper.DragAccess.VERTICAL))
        itemTouchHelper.attachToRecyclerView(fragment_book_main_rv)
    }

    override fun onItemClick(t: BookEntity, v: View) {
        ActivityNavigation.navigateTo(
                context,
                ActivityNavigation.Destination.BookDetail(t.id, t.title),
                getTransitionBundle(v)
        )
    }

    override fun onItemDismissed(t: BookEntity, position: Int) = Unit

    // Do nothing, only react to move actions in the on item move finished method
    override fun onItemMove(t: BookEntity, from: Int, to: Int) = Unit

    override fun onItemMoveFinished() {
        viewModel.updateBookPositions(bookAdapter.data)
    }

    override fun onDelete(b: BookEntity) {
        viewModel.deleteBook(b)
    }

    override fun onShare(b: BookEntity) {
        tracker.trackEvent(DanteTrackingEvent.BookSharedEvent())
        ActivityNavigation.navigateTo(context, ActivityNavigation.Destination.Share(b))
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

    private fun getTransitionBundle(v: View): Bundle? {
        return activity?.let { act ->
            ActivityOptionsCompat
                .makeSceneTransitionAnimation(act,
                        Pair(
                            v.findViewById(R.id.item_book_card),
                            getString(R.string.transition_name_card)
                        ),
                        Pair(v.findViewById(
                            R.id.item_book_img_thumb),
                            getString(R.string.transition_name_thumb)
                        )
                )
                .toBundle()
        }
    }

    private fun updateEmptyView(hide: Boolean, animate: Boolean) {

        val alpha = if (hide) 0f else 1f
        if (animate) {
            fragment_book_main_empty_view.animate()
                    .alpha(alpha)
                    .setDuration(450)
                    .start()
        } else {
            fragment_book_main_empty_view.alpha = (alpha)
        }
    }

    companion object {

        private const val ARG_STATE = "arg_state"

        fun newInstance(state: BookState): MainBookFragment {
            return MainBookFragment().apply {
                this.arguments = Bundle().apply {
                    putSerializable(ARG_STATE, state)
                }
            }
        }
    }
}
