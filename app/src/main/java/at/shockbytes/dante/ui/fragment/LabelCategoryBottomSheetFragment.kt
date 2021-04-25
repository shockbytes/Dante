package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.adapter.SimpleBookAdapter
import at.shockbytes.dante.ui.viewmodel.LabelCategoryViewModel
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.adapter.BaseAdapter
import kotlinx.android.synthetic.main.fragment_label_category_bottom_sheet.*
import javax.inject.Inject

class LabelCategoryBottomSheetFragment : BaseBottomSheetFragment() {

    override val layoutRes: Int = R.layout.fragment_label_category_bottom_sheet

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: LabelCategoryViewModel

    private var label: BookLabel by argument()

    private val simpleBookAdapter: SimpleBookAdapter by lazy {
        SimpleBookAdapter(
            requireContext(),
            imageLoader,
            object : BaseAdapter.OnItemClickListener<BookEntity> {
                override fun onItemClick(content: BookEntity, position: Int, v: View) {
                    ActivityNavigator.navigateTo(
                        context,
                        Destination.BookDetail(
                            Destination.BookDetail.BookDetailInfo(content.id, content.title)
                        ),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
                    )
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestBooksWithLabel(label)
        viewModel.getBooks().observe(this, Observer(::showBooks))
    }

    private fun showBooks(books: List<BookEntity>) {
        simpleBookAdapter.updateData(books)
        tv_label_category_description.text = resources.getQuantityString(R.plurals.books, books.size, books.size)
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {
        rv_label_category.adapter = simpleBookAdapter
        tv_label_category_header.apply {
            text = label.title
            setTextColor(label.labelHexColor.asColorInt())
        }
    }

    companion object {

        fun newInstance(label: BookLabel): LabelCategoryBottomSheetFragment {
            return LabelCategoryBottomSheetFragment().apply {
                this.label = label
            }
        }
    }
}