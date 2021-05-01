package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.FragmentLabelCategoryBottomSheetBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.adapter.SimpleBookAdapter
import at.shockbytes.dante.ui.viewmodel.LabelCategoryViewModel
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.adapter.BaseAdapter
import javax.inject.Inject

class LabelCategoryBottomSheetFragment : BaseBottomSheetFragment<FragmentLabelCategoryBottomSheetBinding>() {

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

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentLabelCategoryBottomSheetBinding {
        return FragmentLabelCategoryBottomSheetBinding.inflate(inflater, root, attachToRoot)
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
        vb.tvLabelCategoryDescription.text = resources.getQuantityString(R.plurals.books, books.size, books.size)
    }

    override fun unbindViewModel() = Unit

    override fun setupViews() {
        vb.rvLabelCategory.adapter = simpleBookAdapter
        vb.tvLabelCategoryHeader.apply {
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