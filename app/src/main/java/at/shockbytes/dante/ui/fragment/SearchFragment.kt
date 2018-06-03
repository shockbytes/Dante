package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BookSearchSuggestionAdapter
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.books.BookSearchSuggestion
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.DetailActivity
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.books.Book
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.OnClick
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import kotterknife.bindView
import java.net.UnknownHostException
import javax.inject.Inject


/**
 * @author Martin Macheiner
 * Date: 03.02.2018.
 */

class SearchFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BookSearchSuggestion> {

    interface OnBookSuggestionDownloadClickListener {

        fun onBookSuggestionClicked(s: BookSearchSuggestion)
    }

    @Inject
    protected lateinit var manager: BookManager

    private val bookTransform: (Book) -> BookSearchSuggestion = { b ->
        BookSearchSuggestion(b.id, b.title, b.author, b.thumbnailAddress, b.isbn)
    }

    private val addClickedListener: (BookSearchSuggestion) -> Unit = { suggestion ->
        DanteUtils.hideKeyboard(activity)
        searchView.setSearchFocused(false)
        downloadClickListener?.onBookSuggestionClicked(suggestion)
    }

    private val searchView: FloatingSearchView by bindView(R.id.fragment_search_search_view)
    private val rvResults: RecyclerView by bindView(R.id.fragment_search_rv)
    private val emptyView: View by bindView(R.id.fragment_search_empty_view)
    private val btnSearchOnline: Button by bindView(R.id.fragment_search_btn_search_online)

    private lateinit var rvAdapter: BookSearchSuggestionAdapter

    private var downloadClickListener: OnBookSuggestionDownloadClickListener? = null

    override val layoutId = R.layout.fragment_search

    override fun setupViews() {

        rvAdapter = BookSearchSuggestionAdapter(context!!, mutableListOf(), addClickedListener)
        rvAdapter.onItemClickListener = this
        rvResults.layoutManager = LinearLayoutManager(context)
        rvResults.adapter = rvAdapter
        val dividerItemDecoration = DividerItemDecoration(rvResults.context,
                DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.recycler_divider)!!)
        rvResults.addItemDecoration(dividerItemDecoration)

        emptyView.visibility = View.GONE

        searchView.setOnHomeActionClickListener {
            activity?.supportFinishAfterTransition()
        }
        searchView.setOnQueryChangeListener { oldQuery, newQuery ->
            if (oldQuery != "" && newQuery == "") {
                searchView.clearSuggestions()
                rvAdapter.clear()
            } else {
                showBooks(newQuery, true)
            }
        }
        searchView.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSearchAction(currentQuery: String?) {
                if (currentQuery != null && !currentQuery.isNullOrEmpty()) {
                    showBooks(currentQuery, true)
                }
            }

            override fun onSuggestionClicked(s: SearchSuggestion?) {
            }
        })
        searchView.setSearchFocused(true)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        downloadClickListener = context as? OnBookSuggestionDownloadClickListener
    }

    override fun onItemClick(t: BookSearchSuggestion, v: View) {
        DanteUtils.hideKeyboard(activity)
        if (t.bookId > -1) {
            startActivity(DetailActivity.newIntent(context!!, t.bookId, t.title))
        }
    }

    @OnClick(R.id.fragment_search_btn_search_online)
    protected fun onClickOnlineSearch() {
        DanteUtils.hideKeyboard(activity)
        showBooks(searchView.query, false)
    }

    private fun showBooks(query: String, keepLocal: Boolean) {

        if (!query.isEmpty()) {
            searchView.showProgress()
            btnSearchOnline.isEnabled = false

            val source = if (keepLocal) localSearch(query) else onlineSearch(query)
            source.bindUntilEvent(this, FragmentEvent.DESTROY)
                    .subscribe({

                        val emptyViewVisibility: Int
                        if (it.isNotEmpty()) {
                            rvAdapter.data = it.toMutableList()
                            rvResults.scrollToPosition(0)
                            emptyViewVisibility = View.GONE
                        } else {
                            rvAdapter.clear()
                            emptyViewVisibility = View.VISIBLE
                        }
                        emptyView.visibility = emptyViewVisibility
                        searchView.hideProgress()
                        btnSearchOnline.isEnabled = true
                    }, {

                        showToast(message4SearchException(it))

                        searchView.clearQuery()
                        emptyView.visibility = View.GONE
                        searchView.hideProgress()
                        btnSearchOnline.isEnabled = true
                    })
        }
    }

    private fun localSearch(query: String): Flowable<List<BookSearchSuggestion>> {
        return manager.searchBooks(query)
                .map { it.map { b -> bookTransform(b) } }
    }

    private fun onlineSearch(query: String): Flowable<List<BookSearchSuggestion>> {
        return manager.downloadBook(query)
                .map { b ->
                    val list = mutableListOf<BookSearchSuggestion>()
                    if (b.hasSuggestions) {
                        // Save to call !! because hasSuggestions already checks nullability
                        list.add(bookTransform(b.mainSuggestion!!))
                        b.otherSuggestions
                                .filter { it.isbn.isNotEmpty() }
                                .mapTo(list, { book -> bookTransform(book) })
                    }
                    list.toList()
                }
                .toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun message4SearchException(t: Throwable): String {
        return when (t) {
            is UnknownHostException -> getString(R.string.no_internet_connection)
            else -> getString(R.string.search_invalid_query)
        }
    }

    companion object {

        fun newInstance(): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

    }

}