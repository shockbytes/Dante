package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.fragment.SettingsFragment

class SettingsActivity : BackNavigableActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager
                .beginTransaction()
                .replace(android.R.id.content, SettingsFragment.newInstance())
                .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) { }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
