package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookSearchItem
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.activity.SearchActivity
import at.shockbytes.dante.ui.adapter.BookSearchSuggestionAdapter
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.ui.viewmodel.SearchViewModel
import at.shockbytes.dante.util.hideKeyboard
import at.shockbytes.dante.util.viewModelOfActivity
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.synthetic.main.fragment_search.*
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
class SearchFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BookSearchItem> {

    override val layoutId = R.layout.fragment_search

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: SearchViewModel

    private val addClickedListener: ((BookSearchItem) -> Unit) = { item ->
        activity?.hideKeyboard()
        fragment_search_searchview.setSearchFocused(false)
        viewModel.requestBookDownload(item)
    }

    private lateinit var rvAdapter: BookSearchSuggestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(activity as SearchActivity, vmFactory)
    }

    override fun setupViews() {

        rvAdapter = BookSearchSuggestionAdapter(fragment_search_rv.context, imageLoader, addClickedListener, onItemClickListener = this)
        fragment_search_rv.layoutManager = LinearLayoutManager(context)
        fragment_search_rv.adapter = rvAdapter
        val dividerItemDecoration = DividerItemDecoration(fragment_search_rv.context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(fragment_search_rv.context, R.drawable.recycler_divider)!!)
        fragment_search_rv.addItemDecoration(dividerItemDecoration)

        fragment_search_empty_view.visibility = View.GONE

        fragment_search_searchview.homeActionClickListener = {
            activity?.supportFinishAfterTransition()
        }
        fragment_search_searchview.queryListener = { newQuery ->
            if (newQuery.toString() == "") {
                rvAdapter.clear()
                viewModel.requestInitialState()
            } else {
                viewModel.showBooks(newQuery, true)
            }
        }
        fragment_search_searchview.setSearchFocused(true)

        fragment_search_btn_search_online.setOnClickListener {
            activity?.hideKeyboard()
            viewModel.showBooks(fragment_search_searchview.currentQuery, false)
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.getSearchState().observe(this, { searchState ->
            when (searchState) {
                is SearchViewModel.SearchState.LoadingState -> {
                    fragment_search_searchview.showProgress(true)
                    fragment_search_btn_search_online.isEnabled = false
                }
                is SearchViewModel.SearchState.EmptyState -> {
                    fragment_search_searchview.showProgress(false)
                    rvAdapter.clear()
                    fragment_search_empty_view.visibility = View.VISIBLE
                    fragment_search_btn_search_online.isEnabled = true
                }
                is SearchViewModel.SearchState.SuccessState -> {
                    fragment_search_searchview.showProgress(false)
                    rvAdapter.data = searchState.items.toMutableList()
                    fragment_search_rv.scrollToPosition(0)
                    fragment_search_empty_view.visibility = View.GONE
                    fragment_search_btn_search_online.isEnabled = true
                }
                is SearchViewModel.SearchState.ErrorState -> {
                    showToast(message4SearchException(searchState.throwable))
                    fragment_search_searchview.clearQuery()
                    fragment_search_searchview.showProgress(false)
                    fragment_search_empty_view.visibility = View.GONE
                    fragment_search_btn_search_online.isEnabled = true
                }
                is SearchViewModel.SearchState.InitialState -> {
                    fragment_search_searchview.showProgress(false)
                    fragment_search_empty_view.visibility = View.GONE
                    fragment_search_btn_search_online.isEnabled = false
                }
            }
        })
    }

    override fun unbindViewModel() = Unit

    override fun onItemClick(content: BookSearchItem, position: Int, v: View) {
        activity?.hideKeyboard()
        if (content.bookId.isValid()) {
            startActivity(DetailActivity.newIntent(requireContext(), content.bookId, content.title))
        }
    }

    private fun message4SearchException(t: Throwable): String {
        return when (t) {
            is UnknownHostException -> getString(R.string.no_internet_connection)
            else -> getString(R.string.search_invalid_query)
        }
    }

    companion object {

        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}