package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent

class TimeLineFragment: BaseFragment() {

    override val layoutId: Int = R.layout.fragment_timeline

    override fun setupViews() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun injectToGraph(appComponent: AppComponent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bindViewModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbindViewModel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        fun newInstance(): TimeLineFragment {
            return TimeLineFragment()
        }
    }
}