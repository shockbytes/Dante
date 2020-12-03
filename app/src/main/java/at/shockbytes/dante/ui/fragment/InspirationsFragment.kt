package at.shockbytes.dante.ui.fragment

import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.InspirationsPagerAdapter
import at.shockbytes.dante.util.setVisible
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dante_toolbar.*
import kotlinx.android.synthetic.main.fragment_inspirations.*

class InspirationsFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_inspirations

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

        vp_fragment_inspirations.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2
        }
    }

    private fun connectTabsAndViewPager() {
        tabs_fragment_inspirations.setupWithViewPager(vp_fragment_inspirations)
    }

    private fun setupTabIcons() {
        getTabAt(0)?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_wishlist)
        getTabAt(1)?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_suggestions)
    }

    private fun getTabAt(index: Int): TabLayout.Tab? {
        return tabs_fragment_inspirations.getTabAt(index)
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
