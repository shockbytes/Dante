package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.StatisticsFragment

class StatisticsActivity : ContainerBackNavigableActivity() {

    override val displayFragment: Fragment
        get() = StatisticsFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) {
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, StatisticsActivity::class.java)
        }
    }
}