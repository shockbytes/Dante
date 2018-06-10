package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent

/**
 * @author  Martin Macheiner
 * Date:    06-Jun-18.
 */

class SuggestionsFragment: BaseFragment() {

    override val layoutId = R.layout.fragment_suggestions

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }


    companion object {

        fun newInstance(): SuggestionsFragment {
            val fragment = SuggestionsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }


    }

}