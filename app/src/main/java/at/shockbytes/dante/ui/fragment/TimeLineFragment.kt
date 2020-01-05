package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

class TimeLineFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_timeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setupViews() {
        // TODO
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        // TODO
    }

    override fun unbindViewModel() {
        // TODO
    }

    companion object {

        fun newInstance(): TimeLineFragment {
            return TimeLineFragment()
        }
    }
}