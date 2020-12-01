package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.ui.adapter.OnSuggestionActionClickedListener
import at.shockbytes.dante.ui.adapter.SuggestionsAdapter
import at.shockbytes.dante.ui.viewmodel.SuggestionsViewModel
import at.shockbytes.dante.util.SharedViewComponents
import at.shockbytes.dante.util.viewModelOf
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
                    // TODO Add to wishlist
                    showToast("Add to wishlist")
                    viewModel.trackAddSuggestionToWishlist(
                        suggestion.suggestionId,
                        suggestion.suggestion.title,
                        suggestion.suggester.name
                    )
                }

                override fun onReportBookSuggestion(suggestionId: String) {
                    showToast("Report suggestion!")
                }
            }
        )
        rv_suggestions.apply {
            layoutManager = SharedViewComponents.layoutManagerForBooks(requireContext())
            this.adapter = suggestionAdapter
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestSuggestions()
        viewModel.getSuggestionState().observe(this, Observer(::handleSuggestionState))
    }

    private fun handleSuggestionState(suggestionsState: SuggestionsViewModel.SuggestionsState) {
        when (suggestionsState) {
            is SuggestionsViewModel.SuggestionsState.Present -> handleSuggestions(suggestionsState.suggestions)
            SuggestionsViewModel.SuggestionsState.Empty -> handleEmptyState()
        }
    }

    private fun handleEmptyState() {
        // TODO Handle empty state! Important for online feature later
    }

    private fun handleSuggestions(suggestions: List<Suggestion>) {
        suggestionAdapter.data = suggestions.toMutableList()
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(): SuggestionsFragment {
            return SuggestionsFragment()
        }
    }
}