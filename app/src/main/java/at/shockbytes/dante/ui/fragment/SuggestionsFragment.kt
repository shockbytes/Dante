package at.shockbytes.dante.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.FragmentSuggestionsBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.ui.adapter.suggestions.OnSuggestionActionClickedListener
import at.shockbytes.dante.ui.adapter.suggestions.SuggestionsAdapter
import at.shockbytes.dante.ui.adapter.suggestions.SuggestionsAdapterItem
import at.shockbytes.dante.ui.viewmodel.SuggestionsViewModel
import at.shockbytes.dante.ui.view.SharedViewComponents
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class SuggestionsFragment : BaseFragment<FragmentSuggestionsBinding>() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: SuggestionsViewModel by lazy { viewModelOf(vmFactory) }

    private lateinit var suggestionAdapter: SuggestionsAdapter

    override fun setupViews() {
        setupToolbar()

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
            onSuggestionExplanationClickedListener = { viewModel.dismissExplanation() }
        )
        vb.rvSuggestions.apply {
            layoutManager = SharedViewComponents.layoutManagerForBooks(requireContext())
            this.adapter = suggestionAdapter
        }
    }

    private fun setupToolbar() {
        with(vb.toolbarWishlist) {
            danteToolbarTitle.setText(R.string.suggestions_title)
            danteToolbarBack.apply {
                setVisible(true)
                setOnClickListener {
                    activity?.onBackPressed()
                }
            }
        }
    }


    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentSuggestionsBinding {
        return FragmentSuggestionsBinding.inflate(inflater, root, attachToRoot)
    }

    private fun showReportBookConfirmation(suggestionId: String, suggestionTitle: String) {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_report_suggestion)
            title(text = getString(R.string.book_report_confirmation_title))
            message(text = getString(R.string.book_report_confirmation_msg, suggestionTitle))
            positiveButton(R.string.report) {
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
            is SuggestionsViewModel.SuggestionsState.Loading -> handleLoadingState()
            is SuggestionsViewModel.SuggestionsState.Present -> handleSuggestions(suggestionsState.suggestions)
            is SuggestionsViewModel.SuggestionsState.Empty -> handleEmptyState()
            is SuggestionsViewModel.SuggestionsState.Error -> handleErrorState()
            is SuggestionsViewModel.SuggestionsState.UnauthenticatedUser -> handleUnauthenticatedState()
        }
    }

    private fun handleSuggestionEvents(event: SuggestionsViewModel.SuggestionEvent) {

        when (event) {
            is SuggestionsViewModel.SuggestionEvent.MoveToWishlistEvent -> {
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

    private fun handleLoadingState() {
        vb.pbSuggestions.setVisible(true)
        vb.rvSuggestions.setVisible(false)
        vb.tvSuggestionsExplanation.setVisible(false)
    }

    private fun handleUnauthenticatedState() {
        handleNonHappyState(R.string.suggestion_view_login_required_message)
    }

    private fun handleEmptyState() {
        handleNonHappyState(R.string.no_suggestions_for_user)
    }

    private fun handleErrorState() {
        handleNonHappyState(R.string.suggestion_error)
    }

    private fun handleNonHappyState(textRes: Int) {
        vb.pbSuggestions.setVisible(false)
        vb.rvSuggestions.setVisible(false)
        vb.tvSuggestionsExplanation.apply {
            setVisible(true)
            setText(textRes)
        }
    }

    private fun handleSuggestions(suggestions: List<SuggestionsAdapterItem>) {
        vb.rvSuggestions.setVisible(true)
        vb.tvSuggestionsExplanation.setVisible(false)
        vb.pbSuggestions.setVisible(false)

        suggestionAdapter.updateData(suggestions)
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance() = SuggestionsFragment()
    }
}