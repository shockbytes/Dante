package at.shockbytes.dante.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.FragmentPickRandomBookBinding
import at.shockbytes.dante.util.arguments.argument
import at.shockbytes.dante.util.arguments.argumentNullable
import javax.inject.Inject

class PickRandomBookFragment : BaseFragment<FragmentPickRandomBookBinding>() {

    @Inject
    protected lateinit var imageLoader: ImageLoader

    private var title by argument<String>()
    private var iconUrl by argumentNullable<String>()

    private var onPickClickListener: (() -> Unit)? = null


    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentPickRandomBookBinding {
        return FragmentPickRandomBookBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        vb.tvRandomPickTitle.text = title
        iconUrl?.let(::loadIcon)

        vb.btnRandomPickPick.setOnClickListener {
            closeFragment()
            onPickClickListener?.invoke()
        }

        vb.btnRandomPickClose.setOnClickListener {
            closeFragment()
        }
    }

    private fun closeFragment() = parentFragmentManager.popBackStack()

    private fun loadIcon(url: String) {
        imageLoader.loadImageWithCornerRadius(
                requireContext(),
                url,
                vb.ivRandomPickCover,
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