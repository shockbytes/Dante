package at.shockbytes.dante.ui.fragment

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_manual_add.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddFragment : BaseFragment(), ImageLoadingCallback {

    override val layoutId = R.layout.fragment_manual_add

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: ManualAddViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)

        arguments?.getParcelable<BookEntity>(ARG_BOOK_ENTITY_UPDATE).let { bookEntity ->
            viewModel.initialize(bookEntity)
        }
    }

    override fun setupViews() {

        imgViewManualAdd.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            activity?.let { a ->
                viewModel.pickImage(a)
            }
        }

        editTextManualAddTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let { title ->
                    (activity as? TintableBackNavigableActivity)?.tintTitle(title.toString())
                }
            }
        })

        btnManualAddUpcoming.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READ_LATER)
        }

        btnManualAddCurrent.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READING)
        }

        btnManualAddDone.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READ)
        }

        btn_update_book_discard.setOnClickListener { v ->
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

        btn_update_book_save.setOnClickListener { v ->
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

    override fun unbindViewModel() {
        // Not needed...
    }

    override fun onImageLoadingFailed(e: Exception?) {
        Timber.e(e)
    }

    override fun onImageResourceReady(resource: Drawable?) {

        (resource as? BitmapDrawable)?.bitmap?.let { bm ->
            androidx.palette.graphics.Palette.from(bm).generate { palette ->

                val actionBarColor = palette?.lightMutedSwatch?.rgb
                val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
                val statusBarColor = palette?.darkMutedSwatch?.rgb

                (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                    actionBarTextColor, statusBarColor)
            }
        }
    }

    private fun setupObserver() {

        viewModel.getImageState().observe(this, { imageState ->

            when (imageState) {
                is ManualAddViewModel.ImageState.ThumbnailUri -> {
                    imageLoader.loadImageUri(
                        requireContext(),
                        imageState.uri,
                        imgViewManualAdd,
                        R.drawable.ic_placeholder,
                        circular = false,
                        callback = this,
                        callbackHandleValues = Pair(first = false, second = true)
                    )
                }
                ManualAddViewModel.ImageState.NoImage -> {
                    imageLoader.loadImageResource(
                        requireContext(),
                        R.drawable.ic_placeholder,
                        imgViewManualAdd
                    )
                }
            }
        })

        viewModel.getViewState().observe(this, { viewState ->

            when (viewState) {
                ManualAddViewModel.ViewState.ManualAdd -> {
                    container_manual_add_buttons.setVisible(true)
                    container_update_book_buttons.setVisible(false)
                }
                is ManualAddViewModel.ViewState.UpdateBook -> {
                    container_manual_add_buttons.setVisible(false)
                    container_update_book_buttons.setVisible(true)
                    populateBookDataViews(viewState.bookEntity)
                }
            }
        })

        viewModel.onAddEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->

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
            .addTo(compositeDisposable)
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

            editTextManualAddTitle.setText(title)
            editTextManualAddSubtitle.setText(subTitle)
            editTextManualAddAuthors.setText(author)
            editTextManualAddPages.setText(pageCount.toString())
            editTextManualAddPublishedDate.setText(publishedDate)
            editTextManualAddIsbn.setText(isbn)
            editTextManualAddSummary.setText(summary)

            val languages = Languages.values()
            val languageIdx = languages.indexOfFirst { it.code == language }

            if (languageIdx > -1) {
                spinnerManualAddLanguage.setSelection(languageIdx, true)
            }
        }
    }

    private fun setupLanguageSpinner() {
        spinnerManualAddLanguage.adapter = ManualAddLanguageSpinnerAdapter(requireContext(), Languages.values())
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
        val title = editTextManualAddTitle.text?.toString()
        val subTitle: String? = editTextManualAddSubtitle.text?.toString()
        val authors = editTextManualAddAuthors.text?.toString()
        val pageCount = editTextManualAddPages.text?.toString()?.toIntOrNull()
        val publishedDate = editTextManualAddPublishedDate.text?.toString()
        val isbn = editTextManualAddIsbn.text?.toString()
        val summary = editTextManualAddSummary.text?.toString()

        val languages = Languages.values()
        val lIdx = spinnerManualAddLanguage.selectedItemPosition.coerceIn(0..languages.size)
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