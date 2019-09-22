package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class SuggestionsFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_suggestions

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        // Not needed...
    }

    override fun unbindViewModel() {
        // Not needed...
    }

    companion object {

        fun newInstance(): SuggestionsFragment {
            return SuggestionsFragment()
        }
    }
}