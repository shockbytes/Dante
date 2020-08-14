package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.arguments.argumentNullable
import kotlinx.android.synthetic.main.fragment_pick_random_book.*
import javax.inject.Inject

class PickRandomBookFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_pick_random_book

    @Inject
    protected lateinit var imageLoader: ImageLoader

    private var title by argument<String>()
    private var iconUrl by argumentNullable<String>()

    private var onPickClickListener: (() -> Unit)? = null

    override fun setupViews() {
        tv_random_pick_title.text = title
        iconUrl?.let(::loadIcon)

        btn_random_pick_pick.setOnClickListener {
            closeFragment()
            onPickClickListener?.invoke()
        }

        btn_random_pick_close.setOnClickListener {
            closeFragment()
        }
    }

    private fun closeFragment() = parentFragmentManager.popBackStack()

    private fun loadIcon(url: String) {
        imageLoader.loadImageWithCornerRadius(
                requireContext(),
                url,
                iv_random_pick_cover,
                R.drawable.ic_placeholder,
                cornerDimension = resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
        )
    }

    fun setOnPickClickListener(listener: () -> Unit): PickRandomBookFragment {
        return apply {
            this.onPickClickListener = listener
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() = Unit

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(title: String, iconUrl: String?): PickRandomBookFragment {
            return PickRandomBookFragment().apply {
                this.title = title
                this.iconUrl = iconUrl
            }
        }
    }
}