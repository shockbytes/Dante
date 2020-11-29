package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import kotlinx.android.synthetic.main.dante_toolbar.*

class InspirationsFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_inspirations

    override fun setupViews() {
        dante_toolbar_title.setText("Works!")
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

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
