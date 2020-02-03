package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.adapter.ManualAddLanguageSpinnerAdapter
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.image.ImageLoadingCallback
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel
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

        imgViewManualAdd.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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

        btnManualAddUpcoming.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READ_LATER)
        }

        btnManualAddCurrent.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READING)
        }

        btnManualAddDone.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            storeBook(BookState.READ)
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

        viewModel.getThumbnailUrl().observe(this, Observer { thumbnailUrl ->
            thumbnailUrl?.let {
                imageLoader.loadImageUri(
                    requireContext(),
                    thumbnailUrl,
                    imgViewManualAdd,
                    R.drawable.ic_placeholder,
                    circular = false,
                    callback = this,
                    callbackHandleValues = Pair(first = false, second = true)
                )
            }
        })

        viewModel.getViewState().observe(this, Observer { viewState ->

            when (viewState) {
                ManualAddViewModel.ViewState.ManualAdd -> {
                    container_manual_add_buttons.setVisible(true)
                }
                is ManualAddViewModel.ViewState.UpdateBook -> {
                    container_manual_add_buttons.setVisible(false)
                    // TODO Populate Views
                }
            }
        })

        viewModel.onAddEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->

                when (event) {
                    is ManualAddViewModel.AddEvent.Success -> {
                        activity?.onBackPressed()
                    }
                    is ManualAddViewModel.AddEvent.Error -> {
                        showSnackbar(getString(R.string.manual_add_error),
                            getString(android.R.string.ok), true) { this.dismiss() }
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    private fun setupLanguageSpinner() {
        context?.let { ctx ->
            val data = buildLanguageData()
            spinnerManualAddLanguage.adapter = ManualAddLanguageSpinnerAdapter(ctx, data, imageLoader)
        }
    }

    private fun buildLanguageData(): Array<ManualAddLanguageSpinnerAdapter.LanguageItem> {

        val languageIds = resources.getStringArray(R.array.language_codes)
        val langNotAvailable = resources.getString(R.string.language_not_available)
        val langEnglish = resources.getString(R.string.language_english)

        return resources.getStringArray(R.array.language_names)
                .mapIndexedNotNull { index, s ->
                    s?.let { language ->
                        val shortName = languageIds[index]

                        val url = if (shortName == langEnglish) {
                            buildFlagIconUrl("gb")
                        } else {
                            buildFlagIconUrl(shortName)
                        }
                        val showFlag = shortName != langNotAvailable

                        ManualAddLanguageSpinnerAdapter.LanguageItem(language, shortName, url, showFlag)
                    }
                }
                .toTypedArray()
    }

    private fun buildFlagIconUrl(id: String, size: Int = 64): String {
        return "https://www.countryflags.io/$id/flat/$size.png"
    }

    private fun storeBook(state: BookState) {

        val title = editTextManualAddTitle.text?.toString()
        val subTitle: String? = editTextManualAddSubtitle.text?.toString()
        val authors = editTextManualAddAuthors.text?.toString()
        val pageCount = editTextManualAddPages.text?.toString()?.toIntOrNull()
        val publishedDate = editTextManualAddPublishedDate.text?.toString()
        val isbn = editTextManualAddIsbn.text?.toString()
        val summary = editTextManualAddSummary.text?.toString()

        val languages = resources.getStringArray(R.array.language_codes)
        val lIdx = spinnerManualAddLanguage.selectedItemPosition.coerceIn(0..languages.size)
        val language = languages[lIdx]

        viewModel.storeBook(
                title,
                authors,
                pageCount,
                state,
                subTitle,
                publishedDate,
                isbn,
                language,
                summary
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