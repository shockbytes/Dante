package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.FragmentInspirationsBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.InspirationsPagerAdapter
import com.google.android.material.tabs.TabLayout

class InspirationsFragment : BaseFragment<FragmentInspirationsBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentInspirationsBinding {
        return FragmentInspirationsBinding.inflate(inflater, root, attachToRoot)
    }

    private lateinit var pagerAdapter: InspirationsPagerAdapter

    override fun setupViews() {
        setupToolbar()
        setupViewPager()
        connectTabsAndViewPager()
        setupTabIcons()
    }

    fun moveToWishlistTab() {
        getTabAt(0)?.select()
    }

    private fun setupToolbar() {
        dante_toolbar_title.setText(R.string.inspirations)
        dante_toolbar_back.apply {
            setVisible(true)
            setOnClickListener {
                activity?.onBackPressed()
            }
        }
    }

    private fun setupViewPager() {
        pagerAdapter = InspirationsPagerAdapter(requireContext(), childFragmentManager)

        vb.vpFragmentInspirations.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2
        }
    }

    private fun connectTabsAndViewPager() {
        vb.tabsFragmentInspirations.setupWithViewPager(vb.vpFragmentInspirations)
    }

    private fun setupTabIcons() {
        getTabAt(0)?.icon = requireContext().getImage(R.drawable.ic_wishlist)
        getTabAt(1)?.icon = requireContext().getImage(R.drawable.ic_suggestions)
    }

    private fun Context.getImage(@DrawableRes drawable: Int): Drawable? {
        return ContextCompat.getDrawable(this, drawable)
    }

    private fun getTabAt(index: Int): TabLayout.Tab? {
        return vb.tabsFragmentInspirations.getTabAt(index)
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): InspirationsFragment {
            return InspirationsFragment()
        }
    }
}
