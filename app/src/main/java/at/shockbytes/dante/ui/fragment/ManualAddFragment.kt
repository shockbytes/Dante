package at.shockbytes.dante.ui.fragment

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.core.book.Languages
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.adapter.ManualAddLanguageSpinnerAdapter
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.image.ImageLoadingCallback
import at.shockbytes.dante.ui.activity.ManualAddActivity
import at.shockbytes.dante.ui.fragment.dialog.SimpleRequestDialogFragment
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel
import at.shockbytes.dante.core.Constants.ACTION_BOOK_CREATED
import at.shockbytes.dante.core.Constants.EXTRA_BOOK_CREATED_STATE
import at.shockbytes.dante.databinding.FragmentManualAddBinding
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddFragment : BaseFragment<FragmentManualAddBinding>(), ImageLoadingCallback {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentManualAddBinding {
        return FragmentManualAddBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: ManualAddViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)

        arguments?.getParcelable<BookEntity>(ARG_BOOK_ENTITY_UPDATE)
            .let(viewModel::initialize)
    }

    override fun setupViews() {

        vb.cardImageManualAdd.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.pickImage(requireActivity())
        }

        vb.editTextManualAddTitle.doOnTextChanged { text, _, _, _ ->
            (activity as? TintableBackNavigableActivity<*>)?.tintTitle(text.toString())
        }

        vb.btnManualAddUpcoming.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READ_LATER)
        }

        vb.btnManualAddCurrent.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READING)
        }

        vb.btnManualAddDone.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READ)
        }

        vb.btnUpdateBookDiscard.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            SimpleRequestDialogFragment
                .newInstance(
                    title = getString(R.string.update_book_discard_title),
                    message = getString(R.string.update_book_discard_message),
                    icon = R.drawable.ic_delete,
                    positiveText = R.string.discard
                )
                .setOnAcceptListener {
                    activity?.onBackPressed()
                }
                .show(childFragmentManager, "tag-discard-book-update-confirmation")
        }

        vb.btnUpdateBookSave.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            updateBook()
        }

        setupLanguageSpinner()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        setupObserver()
    }

    override fun unbindViewModel() = Unit

    override fun onImageLoadingFailed(e: Exception?) = Timber.e(e)

    override fun onImageResourceReady(resource: Drawable?) {
        hideLoadingIndicator()
        colorToolbarFromResource(resource)
    }

    private fun hideLoadingIndicator() {
        vb.imgViewManualAdd.setVisible(true)
        vb.pbManualAddImageUpload.setVisible(false)
    }

    private fun colorToolbarFromResource(resource: Drawable?) {
        (resource as? BitmapDrawable)?.bitmap?.let(Palette::from)?.generate { palette ->

            val actionBarColor = palette?.lightMutedSwatch?.rgb
            val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
            val statusBarColor = palette?.darkMutedSwatch?.rgb

            (activity as? TintableBackNavigableActivity<*>)
                ?.tintSystemBarsWithText(actionBarColor, actionBarTextColor, statusBarColor)
        }
    }

    private fun setupObserver() {

        viewModel.getImageState().observe(this, Observer(::handleImageState))

        viewModel.getImageLoadingState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleImageLoadingState)
            .addTo(compositeDisposable)

        viewModel.getViewState().observe(this, Observer(::handleViewState))

        viewModel.onAddEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleAddEvent)
            .addTo(compositeDisposable)
    }

    private fun handleImageState(imageState: ManualAddViewModel.ImageState) {
        when (imageState) {
            is ManualAddViewModel.ImageState.ThumbnailUri -> {
                imageLoader.loadImageUri(
                    requireContext(),
                    imageState.uri,
                    vb.imgViewManualAdd,
                    R.drawable.ic_placeholder_cover,
                    circular = false,
                    callback = this,
                    callbackHandleValues = Pair(first = false, second = true)
                )
            }
            ManualAddViewModel.ImageState.NoImage -> {
                imageLoader.loadImageResource(
                    requireContext(),
                    R.drawable.ic_placeholder_cover,
                    vb.imgViewManualAdd
                )
            }
        }
    }

    private fun handleImageLoadingState(imageLoadingState: ManualAddViewModel.ImageLoadingState) {
        when (imageLoadingState) {
            is ManualAddViewModel.ImageLoadingState.Loading -> {
                vb.pbManualAddImageUpload.setVisible(true)
                vb.imgViewManualAdd.setVisible(false)
            }
            is ManualAddViewModel.ImageLoadingState.Error -> {
                vb.pbManualAddImageUpload.setVisible(false)
                vb.imgViewManualAdd.setVisible(true)
            }
            ManualAddViewModel.ImageLoadingState.Success -> Unit // Not needed...
        }
    }

    private fun handleViewState(viewState: ManualAddViewModel.ViewState) {
        when (viewState) {
            ManualAddViewModel.ViewState.ManualAdd -> {
                vb.containerManualAddButtons.setVisible(true)
                vb.containerUpdateBookButtons.setVisible(false)
            }
            is ManualAddViewModel.ViewState.UpdateBook -> {
                vb.containerManualAddButtons.setVisible(false)
                vb.containerUpdateBookButtons.setVisible(true)
                populateBookDataViews(viewState.bookEntity)
            }
        }
    }

    private fun handleAddEvent(event: ManualAddViewModel.AddEvent) {
        when (event) {
            is ManualAddViewModel.AddEvent.Success -> {
                activity?.onBackPressed()
                sendBookCreatedBroadcast(event.createdBookState)
            }
            is ManualAddViewModel.AddEvent.Error -> {
                showSnackbar(getString(R.string.manual_add_error),
                    getString(android.R.string.ok), true) { this.dismiss() }
            }
            is ManualAddViewModel.AddEvent.Updated -> {
                sendBookUpdatedBroadcast(event.updateBookState)
                activity?.onBackPressed()
            }
        }
    }

    private fun sendBookCreatedBroadcast(createdBookState: BookState) {
        sendBroadcast(
            Intent(ACTION_BOOK_CREATED).putExtra(EXTRA_BOOK_CREATED_STATE, createdBookState)
        )
    }

    private fun sendBookUpdatedBroadcast(bookState: BookState) {
        sendBroadcast(
            Intent(BookDetailFragment.ACTION_BOOK_CHANGED)
                .putExtra(ManualAddActivity.EXTRA_UPDATED_BOOK_STATE, bookState)
        )
    }

    private fun sendBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(requireContext())
            .sendBroadcast(intent)
    }

    private fun populateBookDataViews(bookEntity: BookEntity) {
        with(bookEntity) {

            vb.editTextManualAddTitle.setText(title)
            vb.editTextManualAddSubtitle.setText(subTitle)
            vb.editTextManualAddAuthors.setText(author)
            vb.editTextManualAddPages.setText(pageCount.toString())
            vb.editTextManualAddPublishedDate.setText(publishedDate)
            vb.editTextManualAddIsbn.setText(isbn)
            vb.editTextManualAddSummary.setText(summary)

            val languages = Languages.values()
            val languageIdx = languages.indexOfFirst { it.code == language }

            if (languageIdx > -1) {
                vb.spinnerManualAddLanguage.setSelection(languageIdx, true)
            }
        }
    }

    private fun setupLanguageSpinner() {
        vb.spinnerManualAddLanguage.adapter = ManualAddLanguageSpinnerAdapter(
            requireContext(),
            Languages.values()
        )
    }

    private fun updateBook() {
        viewModel.updateBook(gatherBookUpdateData())
    }

    private fun storeBook(state: BookState) {
        viewModel.storeBook(
            gatherBookUpdateData(),
            state
        )
    }

    private fun gatherBookUpdateData(): ManualAddViewModel.BookUpdateData {
        val title = vb.editTextManualAddTitle.text?.toString()
        val subTitle: String? = vb.editTextManualAddSubtitle.text?.toString()
        val authors = vb.editTextManualAddAuthors.text?.toString()
        val pageCount = vb.editTextManualAddPages.text?.toString()?.toIntOrNull()
        val publishedDate = vb.editTextManualAddPublishedDate.text?.toString()
        val isbn = vb.editTextManualAddIsbn.text?.toString()
        val summary = vb.editTextManualAddSummary.text?.toString()

        val languages = Languages.values()
        val lIdx = vb.spinnerManualAddLanguage.selectedItemPosition.coerceIn(0..languages.size)
        val language = languages[lIdx].code

        return ManualAddViewModel.BookUpdateData(
            title = title,
            author = authors,
            pageCount = pageCount,
            subTitle = subTitle,
            publishedDate = publishedDate,
            isbn = isbn,
            language = language,
            summary = summary,
            thumbnailAddress = viewModel.getImageUri()
        )
    }

    companion object {

        private const val ARG_BOOK_ENTITY_UPDATE = "arg_book_entity_update"

        fun newInstance(updatedBookEntity: BookEntity?): ManualAddFragment {
            return ManualAddFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BOOK_ENTITY_UPDATE, updatedBookEntity)
                }
            }
        }
    }
}