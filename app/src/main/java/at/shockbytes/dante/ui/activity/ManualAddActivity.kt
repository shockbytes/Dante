package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerTintableBackNavigableActivity
import at.shockbytes.dante.ui.fragment.ManualAddFragment

/**
 * Author:  Martin Macheiner
 * Date:    30.08.2018
 */
class ManualAddActivity : ContainerTintableBackNavigableActivity() {

    override val displayFragment: Fragment
        get() = ManualAddFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) {}

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, ManualAddActivity::class.java)
        }
    }
}