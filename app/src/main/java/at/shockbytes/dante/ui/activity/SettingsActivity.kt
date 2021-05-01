package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import androidx.viewbinding.ViewBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.SettingsFragment

class SettingsActivity<V: ViewBinding> : ContainerBackNavigableActivity<V>() {

    override val displayFragment = SettingsFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}
