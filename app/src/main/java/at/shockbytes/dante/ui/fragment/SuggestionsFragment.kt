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
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
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

        viewModel.onMoveToWishlistEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bookTitle ->
                (parentFragment as? InspirationsFragment)?.moveToWishlistTab()
                showSnackbar(getString(R.string.book_added_to_wishlist, bookTitle))
            }
            .addTo(compositeDisposable)
    }

    private fun handleSuggestionState(suggestionsState: SuggestionsViewModel.SuggestionsState) {
        when (suggestionsState) {
            is SuggestionsViewModel.SuggestionsState.Present -> handleSuggestions(suggestionsState.suggestions)
            SuggestionsViewModel.SuggestionsState.Empty -> handleEmptyState()
        }
    }

    private fun handleEmptyState() {
        rv_suggestions.setVisible(false)
        tv_suggestions_empty.setVisible(true)
    }

    private fun handleSuggestions(suggestions: List<Suggestion>) {
        rv_suggestions.setVisible(true)
        tv_suggestions_empty.setVisible(false)

        suggestionAdapter.data = suggestions.toMutableList()
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(): SuggestionsFragment {
            return SuggestionsFragment()
        }
    }
}