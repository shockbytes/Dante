package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookState
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.TintableBackNavigableActivity
import at.shockbytes.dante.ui.adapter.ManualAddLanguageSpinnerAdapter
import at.shockbytes.dante.ui.image.ImageLoader
import at.shockbytes.dante.ui.image.ImageLoadingCallback
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel
import at.shockbytes.dante.util.addTo
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
        viewModel = ViewModelProviders.of(this, vmFactory)[ManualAddViewModel::class.java]
        viewModel.reset()
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
            Palette.from(bm).generate { palette ->

                val actionBarColor = palette?.lightMutedSwatch?.rgb
                val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
                val statusBarColor = palette?.darkMutedSwatch?.rgb

                (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                        actionBarTextColor, statusBarColor)
            }
        }
    }

    private fun setupObserver() {

        viewModel.thumbnailUrl.observe(this, Observer {
            context?.let { ctx ->
                it?.let { uri ->
                    imageLoader.loadImageUri(
                        ctx,
                        uri,
                        imgViewManualAdd,
                        R.drawable.ic_placeholder,
                        false,
                        this,
                        Pair(first = false, second = true)
                    )
                }
            }
        })

        viewModel.addEvent.subscribe { event ->

            when (event) {
                is ManualAddViewModel.AddEvent.SuccessEvent -> {
                    activity?.onBackPressed()
                }
                is ManualAddViewModel.AddEvent.ErrorEvent -> {
                    showSnackbar(getString(R.string.manual_add_error),
                            getString(android.R.string.ok), true) { this.dismiss() }
                }
            }
        }.addTo(compositeDisposable)
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

        fun newInstance(): ManualAddFragment {
            return ManualAddFragment()
        }
    }
}