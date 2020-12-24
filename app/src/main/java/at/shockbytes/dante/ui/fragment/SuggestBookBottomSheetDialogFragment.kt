package at.shockbytes.dante.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.core.ui.NegativeDrawable
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.isNightModeEnabled
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_suggest_book.*
import javax.inject.Inject

class SuggestBookBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    private var bookEntity: BookEntity by argument()

    private var onRecommendationEnteredListener: ((String) -> Unit)? = null

    override fun getTheme() = R.style.BottomSheetDialogTheme

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as DanteApp).appComponent.inject(this)
    }

    override fun onResume() {
        super.onResume()

        setBookImageAndTitle()
        setBackgroundImage()
        checkRecommendationInput()
        setupConfirmButtonListener()
    }

    private fun setupConfirmButtonListener() {
        btn_suggest_book_confirm.setOnClickListener {
            editTextEnterSuggestion.text?.toString()?.let { text ->
                onRecommendationEnteredListener?.invoke(text.trim())
                dismiss()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    private fun setBookImageAndTitle() {

        tv_suggest_book_title.text = bookEntity.title

        val imageUrl = bookEntity.normalizedThumbnailUrl
        if (!imageUrl.isNullOrEmpty()) {
            imageLoader.loadImageWithCornerRadius(
                requireContext(),
                imageUrl,
                iv_suggest_book_cover,
                cornerDimension = requireContext().resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
            )
        } else {
            iv_suggest_book_cover.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setBackgroundImage() {
        ContextCompat.getDrawable(requireContext(), R.drawable.suggestion_background)?.let {
            val drawable = if (requireContext().isNightModeEnabled()) {
                NegativeDrawable.ofDrawable(it).drawable
            } else it

            iv_suggestion_cover_background.setImageDrawable(drawable)
        }
    }

    private fun checkRecommendationInput() {
        RxTextView.textChanges(editTextEnterSuggestion)
            .map { text ->
                // Do not allow more than 10 line breaks
                text.count() in 1 until MAX_CHARS && text.count { it == '\n' } < 10
            }
            .subscribe(btn_suggest_book_confirm::setEnabled)
            .addTo(compositeDisposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_suggest_book, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    fun setOnRecommendationEnteredListener(
        function: (String) -> Unit
    ): SuggestBookBottomSheetDialogFragment {
        return this.apply {
            onRecommendationEnteredListener = function
        }
    }

    companion object {

        private const val MAX_CHARS = 180

        fun newInstance(
            bookEntity: BookEntity
        ): SuggestBookBottomSheetDialogFragment {
            return SuggestBookBottomSheetDialogFragment().apply {
                this.bookEntity = bookEntity
            }
        }
    }
}