package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.InspirationsPagerAdapter
import at.shockbytes.dante.util.setVisible
import kotlinx.android.synthetic.main.dante_toolbar.*
import kotlinx.android.synthetic.main.fragment_inspirations.*

class InspirationsFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_inspirations

    override fun setupViews() {
        setupToolbar()
        setupViewPager()
        connectTabsAndViewPager()
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
        val pagerAdapter = InspirationsPagerAdapter(requireContext(), childFragmentManager)

        vp_fragment_inspirations.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2
        }
    }

    private fun connectTabsAndViewPager() {
        tabs_fragment_inspirations.setupWithViewPager(vp_fragment_inspirations)
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
