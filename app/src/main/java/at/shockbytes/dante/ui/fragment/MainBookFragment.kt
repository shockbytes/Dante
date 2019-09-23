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
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.adapter.BookAdapter
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.ui.adapter.OnBookActionClickedListener
import at.shockbytes.dante.ui.viewmodel.BookListViewModel
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import kotlinx.android.synthetic.main.fragment_book_main.*
import javax.inject.Inject

class MainBookFragment :
    BaseFragment(),
    BaseAdapter.OnItemClickListener<BookEntity>,
    BaseAdapter.OnItemMoveListener<BookEntity>,
    OnBookActionClickedListener {

    override val layoutId = R.layout.fragment_book_main

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

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

        fragment_book_main_empty_view.text = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]

        bookAdapter = BookAdapter(fragment_book_main_rv, imageLoader, this).apply {
            onItemClickListener = this@MainBookFragment
            onItemMoveListener = this@MainBookFragment
        }

        fragment_book_main_rv.apply {
            layoutManager = rvLayoutManager
            adapter = bookAdapter
        }

        val itemTouchHelper = ItemTouchHelper(
            BaseItemTouchHelper(bookAdapter,
                false,
                BaseItemTouchHelper.DragAccess.VERTICAL)
        )
        itemTouchHelper.attachToRecyclerView(fragment_book_main_rv)
    }

    override fun onItemClick(t: BookEntity, v: View) {
        ActivityNavigator.navigateTo(
                context,
                Destination.BookDetail(
                    Destination.BookDetail.BookDetailInfo(t.id, t.title)
                ),
                getTransitionBundle(v)
        )
    }

    override fun onItemDismissed(t: BookEntity, position: Int) = Unit

    // Do nothing, only react to move actions in the on item move finished method
    override fun onItemMove(t: BookEntity, from: Int, to: Int) = Unit

    override fun onItemMoveFinished() = viewModel.updateBookPositions(bookAdapter.data)

    override fun onDelete(book: BookEntity) = viewModel.deleteBook(book)

    override fun onShare(book: BookEntity) = ActivityNavigator.navigateTo(context, Destination.Share(book))

    override fun onMoveToUpcoming(book: BookEntity) = viewModel.moveBookToUpcomingList(book)

    override fun onMoveToCurrent(book: BookEntity) = viewModel.moveBookToCurrentList(book)

    override fun onMoveToDone(book: BookEntity) = viewModel.moveBookToDoneList(book)

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
