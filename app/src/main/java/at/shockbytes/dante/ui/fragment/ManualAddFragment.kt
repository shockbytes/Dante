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
import at.shockbytes.dante.ui.viewmodel.ManualAddViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_manual_add.*
import java.security.acl.Owner
import javax.inject.Inject

/**
 * @author  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddFragment : BaseFragment(), RequestListener<Drawable> {

    override val layoutId = R.layout.fragment_manual_add

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory


    private lateinit var viewModel: ManualAddViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[ManualAddViewModel::class.java]
        viewModel.reset()
    }

    override fun setupViews() {
        // TODO Setup spinner with languages

        imgViewManualAdd.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            activity?.fragmentManager?.let { fragmentManager ->
                viewModel.pickImage(fragmentManager)
            }
        }

        editTextManualAddTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let { title ->
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

        setupObserver()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onLoadFailed(e: GlideException?, model: Any?,
                              target: Target<Drawable>?, isFirstResource: Boolean): Boolean = true

    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?,
                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {

        (resource as? BitmapDrawable)?.bitmap?.let { bm ->
            Palette.from(bm).generate { palette ->

                val actionBarColor = palette?.lightMutedSwatch?.rgb
                val actionBarTextColor = palette?.lightMutedSwatch?.titleTextColor
                val statusBarColor = palette?.darkMutedSwatch?.rgb

                (activity as? TintableBackNavigableActivity)?.tintSystemBarsWithText(actionBarColor,
                        actionBarTextColor, statusBarColor)
            }
        }
        return false
    }

    private fun setupObserver() {

        viewModel.imageEvent.observe(this, Observer { uri ->
            Glide.with(context!!).load(uri)
                    .listener(this)
                    .into(imgViewManualAdd)
        })

        viewModel.addEvent.observe(this, Observer { event ->

            when (event) {
                is ManualAddViewModel.AddEvent.SuccessEvent -> {
                    activity?.supportFinishAfterTransition()
                }
                is ManualAddViewModel.AddEvent.ErrorEvent -> {
                    showSnackbar(getString(R.string.manual_add_error),
                            getString(android.R.string.ok), true) { this.dismiss() }
                }
            }
        })
    }

    private fun storeBook(state: BookState) {

        val title = editTextManualAddTitle.text?.toString()
        val subTitle: String? = editTextManualAddSubtitle.text?.toString()
        val authors = editTextManualAddAuthors.text?.toString()
        val pageCount = editTextManualAddPages.text?.toString()?.toIntOrNull()
        val publishedDate = editTextManualAddPublishedDate.text?.toString()
        val isbn = editTextManualAddIsbn.text?.toString()
        val language = spinnerManualAddLanguage.selectedItem as? String

        viewModel.storeBook(title, authors, pageCount, state, subTitle, publishedDate, isbn, language)
    }

    companion object {

        fun newInstance(): ManualAddFragment {
            return ManualAddFragment()
        }

    }

}