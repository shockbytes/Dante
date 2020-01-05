package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.TimeLineFragment

class TimeLineActivity : ContainerBackNavigableActivity() {

    override val displayFragment: Fragment
        get() = TimeLineFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) = Unit

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timeline, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_timeline_help) {
            showToast("TODO Show help")
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, TimeLineActivity::class.java)
        }
    }
}