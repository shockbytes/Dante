package at.shockbytes.dante.ui.fragment

import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent

class AnnouncementFragment : BaseFragment() {
    override val layoutId: Int = R.layout.fragment_announcement

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

        fun newInstance(): AnnouncementFragment {
            return AnnouncementFragment()
        }
    }
}