package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.ui.adapter.suggestions.OnSuggestionActionClickedListener
import at.shockbytes.dante.ui.adapter.suggestions.OnSuggestionExplanationClickedListener
import at.shockbytes.dante.ui.adapter.suggestions.SuggestionsAdapter
import at.shockbytes.dante.ui.adapter.suggestions.SuggestionsAdapterItem
import at.shockbytes.dante.ui.viewmodel.SuggestionsViewModel
import at.shockbytes.dante.util.SharedViewComponents
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_suggestions.*
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class SuggestionsFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_suggestions

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: SuggestionsViewModel by lazy { viewModelOf(vmFactory) }

    private lateinit var suggestionAdapter: SuggestionsAdapter

    override fun setupViews() {

        suggestionAdapter = SuggestionsAdapter(
            requireContext(),
            imageLoader,
            onSuggestionActionClickedListener = object : OnSuggestionActionClickedListener {
                override fun onAddSuggestionToWishlist(suggestion: Suggestion) {
                    viewModel.addSuggestionToWishlist(suggestion)
                }

                override fun onReportBookSuggestion(suggestionId: String, suggestionTitle: String) {
                    showReportBookConfirmation(suggestionId, suggestionTitle)
                }
            },
            onSuggestionExplanationClickedListener = object : OnSuggestionExplanationClickedListener {
                override fun onDismissClicked() = viewModel.dismissExplanation()
                override fun onWantToSuggestClicked() = viewModel.wantToSuggestBooks()
            }
        )
        rv_suggestions.apply {
            layoutManager = SharedViewComponents.layoutManagerForBooks(requireContext())
            this.adapter = suggestionAdapter
        }
    }

    private fun showReportBookConfirmation(suggestionId: String, suggestionTitle: String) {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_report_suggestion)
            title(text = getString(R.string.book_report_confirmation_title))
            message(text = getString(R.string.book_report_confirmation_msg, suggestionTitle))
            positiveButton(R.string.action_delete) {
                viewModel.reportBookSuggestion(suggestionId, suggestionTitle)
            }
            negativeButton(android.R.string.cancel) {
                dismiss()
            }
            cancelOnTouchOutside(false)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestSuggestions()
        viewModel.getSuggestionState().observe(this, Observer(::handleSuggestionState))

        viewModel.onSuggestionEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleSuggestionEvents)
            .addTo(compositeDisposable)
    }

    private fun handleSuggestionState(suggestionsState: SuggestionsViewModel.SuggestionsState) {
        when (suggestionsState) {
            is SuggestionsViewModel.SuggestionsState.Present -> handleSuggestions(suggestionsState.suggestions)
            SuggestionsViewModel.SuggestionsState.Empty -> handleEmptyState()
            SuggestionsViewModel.SuggestionsState.Error -> handleErrorState()
        }
    }

    private fun handleSuggestionEvents(event: SuggestionsViewModel.SuggestionEvent) {

        when (event) {
            is SuggestionsViewModel.SuggestionEvent.MoveToWishlistEvent -> {
                (parentFragment as? InspirationsFragment)?.moveToWishlistTab()
                showSnackbar(getString(R.string.book_added_to_wishlist, event.title))
            }
            is SuggestionsViewModel.SuggestionEvent.ReportSuggestionEvent.Success -> {
                showSnackbar(getString(R.string.book_reported_success, event.title))
            }
            is SuggestionsViewModel.SuggestionEvent.ReportSuggestionEvent.Error -> {
                showSnackbar(getString(R.string.book_reported_error, event.title))
            }
        }
    }

    private fun handleEmptyState() {
        rv_suggestions.setVisible(false)
        tv_suggestions_explanation.apply {
            setVisible(true)
            setText(R.string.no_suggestions_for_user)
        }
    }

    private fun handleErrorState() {
        rv_suggestions.setVisible(false)
        tv_suggestions_explanation.apply {
            setVisible(true)
            setText(R.string.suggestion_error)
        }
    }

    private fun handleSuggestions(suggestions: List<SuggestionsAdapterItem>) {
        rv_suggestions.setVisible(true)
        tv_suggestions_explanation.setVisible(false)

        suggestionAdapter.updateData(suggestions)
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(): SuggestionsFragment {
            return SuggestionsFragment()
        }
    }
}