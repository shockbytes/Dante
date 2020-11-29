package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.suggestions.Suggestion
import at.shockbytes.dante.ui.viewmodel.SuggestionsViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class SuggestionsFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_suggestions

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private val viewModel: SuggestionsViewModel by lazy { viewModelOf(vmFactory) }

    override fun setupViews() {
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
        // TODO
    }

    private fun handleSuggestions(suggestions: List<Suggestion>) {
        // TODO
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(): SuggestionsFragment {
            return SuggestionsFragment()
        }
    }
}