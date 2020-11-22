package at.shockbytes.dante.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.navigation.Destination.BookDetail.BookDetailInfo
import at.shockbytes.dante.ui.adapter.main.BookAdapter
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.flagging.FeatureFlagging
import at.shockbytes.dante.ui.activity.ManualAddActivity.Companion.EXTRA_UPDATED_BOOK_STATE
import at.shockbytes.dante.ui.adapter.OnBookActionClickedListener
import at.shockbytes.dante.ui.adapter.main.BookAdapterEntity
import at.shockbytes.dante.ui.adapter.main.RandomPickCallback
import at.shockbytes.dante.ui.fragment.BookDetailFragment.Companion.ACTION_BOOK_CHANGED
import at.shockbytes.dante.ui.viewmodel.BookListViewModel
import at.shockbytes.dante.core.Constants.ACTION_BOOK_CREATED
import at.shockbytes.dante.core.Constants.EXTRA_BOOK_CREATED_STATE
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.runDelayed
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_book_main.*
import timber.log.Timber
import javax.inject.Inject

class MainBookFragment : BaseFragment(),
    BaseAdapter.OnItemClickListener<BookAdapterEntity>,
    BaseAdapter.OnItemMoveListener<BookAdapterEntity>,
    OnBookActionClickedListener {

    override val layoutId = R.layout.fragment_book_main

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var featureFlagging: FeatureFlagging

    private lateinit var bookState: BookState
    private lateinit var bookAdapter: BookAdapter
    private lateinit var viewModel: BookListViewModel

    private val onLabelClickedListener: ((BookLabel) -> Unit) = { label ->
        LabelCategoryBottomSheetFragment.newInstance(label)
            .show(childFragmentManager, "show-label-bottom-sheet")
    }

    private val onBookOverflowClickedListener: ((BookEntity) -> Unit) = { book ->
        BookActionBottomSheetFragment.newInstance(book)
            .show(childFragmentManager, "book-action-bottom-sheet")
    }

    private val randomPickCallback = object : RandomPickCallback {
        override fun onDismiss() {
            showToast(R.string.random_pick_restore_instruction)
            viewModel.onDismissRandomBookPicker()
            bookAdapter.deleteEntity(BookAdapterEntity.RandomPick)
        }

        override fun onRandomPickClicked() {
            viewModel.pickRandomBookToRead()
        }
    }

    private val bookUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, data: Intent?) {
            when (data?.action) {
                ACTION_BOOK_CHANGED -> handleBookUpdatedBroadcast(data)
                ACTION_BOOK_CREATED -> handleBookCreatedBroadcast(data)
            }
        }
    }

    private fun handleBookUpdatedBroadcast(data: Intent) {
        (data.getSerializableExtra(EXTRA_UPDATED_BOOK_STATE) as? BookState)
            ?.let { updatedBookState ->
                viewModel.onBookUpdatedEvent(updatedBookState)
            }
    }

    private fun handleBookCreatedBroadcast(data: Intent) {
        (data.getSerializableExtra(EXTRA_BOOK_CREATED_STATE) as? BookState)
            ?.let { createdBookState ->
                if (viewModel.state == createdBookState) {
                    runDelayed(500) {
                        fragment_book_main_rv.smoothScrollToPosition(0)
                    }
                }
            }
    }

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
        viewModel = viewModelOf(vmFactory)

        bookState = arguments?.getSerializable(ARG_STATE) as BookState
        viewModel.state = bookState

        registerBookUpdatedBroadcastReceiver()
    }

    private fun registerBookUpdatedBroadcastReceiver() {

        val intentFilter = IntentFilter().apply {
            addAction(ACTION_BOOK_CREATED)
            addAction(ACTION_BOOK_CHANGED)
        }

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(bookUpdatedReceiver, intentFilter)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()
        fragment_book_main_rv.suppressLayout(false)
    }

    override fun onPause() {
        super.onPause()
        fragment_book_main_rv.suppressLayout(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(bookUpdatedReceiver)
    }

    override fun bindViewModel() {
        viewModel.getBooks().observe(this, Observer(::handleBookLoadingState))

        viewModel.onPickRandomBookEvent
            .subscribe(::handleRandomPickEvent)
            .addTo(compositeDisposable)
    }

    private fun handleBookLoadingState(state: BookListViewModel.BookLoadingState) {

        when (state) {
            is BookListViewModel.BookLoadingState.Success -> {
                updateEmptyView(hide = true, animate = false)
                bookAdapter.updateData(state.books)
            }

            is BookListViewModel.BookLoadingState.Empty -> {
                updateEmptyView(hide = false, animate = true)
            }

            is BookListViewModel.BookLoadingState.Error -> {
                showSnackbar(getString(R.string.load_error), showLong = true)
            }
        }
    }

    private fun handleRandomPickEvent(event: BookListViewModel.RandomPickEvent) {

        when (event) {
            is BookListViewModel.RandomPickEvent.RandomPick -> {

                PickRandomBookFragment
                    .newInstance(event.book.title, event.book.normalizedThumbnailUrl)
                    .setOnPickClickListener {
                        viewModel.moveBookToCurrentList(event.book)
                    }
                    .let { fragment ->
                        DanteUtils.addFragmentToActivity(
                            parentFragmentManager,
                            fragment,
                            android.R.id.content,
                            true
                        )
                    }
            }
            BookListViewModel.RandomPickEvent.NoBookAvailable -> {
                Timber.e(IllegalStateException("No book available in random pick event! Should never happen!"))
            }
        }
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {

        fragment_book_main_empty_view.text = resources.getStringArray(R.array.empty_indicators)[bookState.ordinal]

        bookAdapter = BookAdapter(
            requireContext(),
            imageLoader,
            onOverflowActionClickedListener = onBookOverflowClickedListener,
            onItemClickListener = this,
            onItemMoveListener = this,
            onLabelClickedListener = onLabelClickedListener,
            randomPickCallback = randomPickCallback
        )

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

    override fun onItemClick(content: BookAdapterEntity, position: Int, v: View) {

        when (content) {
            is BookAdapterEntity.Book -> {
                ActivityNavigator.navigateTo(
                    context,
                    Destination.BookDetail(BookDetailInfo(content.id, content.title)),
                    getTransitionBundle(v)
                )
            }
            BookAdapterEntity.RandomPick -> Unit // Do nothing
        }
    }

    override fun onItemDismissed(t: BookAdapterEntity, position: Int) = Unit

    // Do nothing, only react to move actions in the on item move finished method
    override fun onItemMove(t: BookAdapterEntity, from: Int, to: Int) = Unit

    override fun onItemMoveFinished() = viewModel.updateBookPositions(bookAdapter.data)

    override fun onDelete(book: BookEntity, onDeletionConfirmed: (Boolean) -> Unit) {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_delete)
            title(text = getString(R.string.ask_for_book_deletion))
            message(text = getString(R.string.ask_for_book_deletion_msg, book.title))
            positiveButton(R.string.action_delete) {
                onDeletionConfirmed(true)
                viewModel.deleteBook(book)
                bookAdapter.deleteEntity(book.toAdapterEntity())
            }
            negativeButton(android.R.string.cancel) {
                onDeletionConfirmed(false)
                dismiss()
            }
            cancelOnTouchOutside(false)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    override fun onShare(book: BookEntity) {
        ActivityNavigator.navigateTo(context, Destination.Share(book))
    }

    override fun onEdit(book: BookEntity) {
        ActivityNavigator.navigateTo(
            context,
            Destination.ManualAdd(book),
            ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
        )
    }

    override fun onMoveToUpcoming(book: BookEntity) {
        viewModel.moveBookToUpcomingList(book)
        bookAdapter.deleteEntity(book.toAdapterEntity())
    }

    override fun onMoveToCurrent(book: BookEntity) {
        viewModel.moveBookToCurrentList(book)
        bookAdapter.deleteEntity(book.toAdapterEntity())
    }

    override fun onMoveToDone(book: BookEntity) {
        viewModel.moveBookToDoneList(book)
        bookAdapter.deleteEntity(book.toAdapterEntity())
    }

    // --------------------------------------------------------------

    private fun getTransitionBundle(v: View): Bundle? {
        return ActivityOptionsCompat
            .makeSceneTransitionAnimation(requireActivity(),
                Pair(
                    v.findViewById(R.id.item_book_card),
                    getString(R.string.transition_name_card)
                ),
                Pair(
                    v.findViewById(R.id.item_book_img_thumb),
                    getString(R.string.transition_name_thumb)
                )
            )
            .toBundle()
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

    private fun BookEntity.toAdapterEntity(): BookAdapterEntity = BookAdapterEntity.Book(this)

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
