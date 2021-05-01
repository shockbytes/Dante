package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookIds
import at.shockbytes.dante.core.book.BookSearchItem
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.injection.ViewModelFactory
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.ui.activity.SearchActivity
import at.shockbytes.dante.ui.adapter.BookSearchSuggestionAdapter
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.FragmentSearchBinding
import at.shockbytes.dante.ui.viewmodel.SearchViewModel
import at.shockbytes.dante.util.hideKeyboard
import at.shockbytes.dante.util.viewModelOfActivity
import at.shockbytes.util.adapter.BaseAdapter
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    03.02.2018
 */
class SearchFragment : BaseFragment<FragmentSearchBinding>(), BaseAdapter.OnItemClickListener<BookSearchItem> {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var vmFactory: ViewModelFactory

    private lateinit var viewModel: SearchViewModel

    private val addClickedListener: ((BookSearchItem) -> Unit) = { item ->
        activity?.hideKeyboard()
        vb.fragmentSearchSearchview.setSearchFocused(false)
        viewModel.requestBookDownload(item)
    }

    private lateinit var rvAdapter: BookSearchSuggestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(activity as SearchActivity, vmFactory)
    }

    override fun setupViews() {

        requireContext().let { ctx ->
            rvAdapter = BookSearchSuggestionAdapter(ctx, imageLoader, addClickedListener, onItemClickListener = this)
            vb.fragmentSearchRv.layoutManager = LinearLayoutManager(context)
            vb.fragmentSearchRv.adapter = rvAdapter
            val dividerItemDecoration = DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(ctx, R.drawable.recycler_divider)!!)
            vb.fragmentSearchRv.addItemDecoration(dividerItemDecoration)
        }


        vb.fragmentSearchEmptyView.visibility = View.GONE

        vb.fragmentSearchSearchview.apply {
            homeActionClickListener = {
                activity?.supportFinishAfterTransition()
            }
            queryListener = { newQuery ->
                if (newQuery.toString() == "") {
                    rvAdapter.clear()
                    viewModel.requestInitialState()
                } else {
                    viewModel.showBooks(newQuery, keepLocal = true)
                }
            }
            setSearchFocused(true)
        }

        vb.fragmentSearchBtnSearchOnline.setOnClickListener {
            activity?.hideKeyboard()
            viewModel.showBooks(vb.fragmentSearchSearchview.currentQuery, keepLocal = false)
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {

        viewModel.getSearchState().observe(this, { searchState ->
            when (searchState) {
                is SearchViewModel.SearchState.LoadingState -> {
                    vb.fragmentSearchSearchview.showProgress(true)
                    vb.fragmentSearchBtnSearchOnline.isEnabled = false
                }
                is SearchViewModel.SearchState.EmptyState -> {
                    vb.fragmentSearchSearchview.showProgress(false)
                    rvAdapter.clear()
                    vb.fragmentSearchEmptyView.visibility = View.VISIBLE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = true
                }
                is SearchViewModel.SearchState.SuccessState -> {
                    vb.fragmentSearchSearchview.showProgress(false)
                    rvAdapter.data = searchState.items.toMutableList()
                    vb.fragmentSearchRv.scrollToPosition(0)
                    vb.fragmentSearchEmptyView.visibility = View.GONE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = true
                }
                is SearchViewModel.SearchState.ErrorState -> {
                    showToast(message4SearchException(searchState.throwable))
                    vb.fragmentSearchSearchview.apply {
                        clearQuery()
                        showProgress(false)
                    }
                    vb.fragmentSearchEmptyView.visibility = View.GONE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = true
                }
                is SearchViewModel.SearchState.InitialState -> {
                    vb.fragmentSearchSearchview.showProgress(false)
                    vb.fragmentSearchEmptyView.visibility = View.GONE
                    vb.fragmentSearchBtnSearchOnline.isEnabled = false
                }
            }
        })
    }

    override fun unbindViewModel() = Unit

    override fun onItemClick(content: BookSearchItem, position: Int, v: View) {
        activity?.hideKeyboard()
        if (BookIds.isValid(content.bookId)) {
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