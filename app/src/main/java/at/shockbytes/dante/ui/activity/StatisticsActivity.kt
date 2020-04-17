package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.activity.core.ContainerBackNavigableActivity
import at.shockbytes.dante.ui.fragment.StatisticsFragment

class StatisticsActivity : ContainerBackNavigableActivity() {

    override val displayFragment = StatisticsFragment.newInstance()

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, StatisticsActivity::class.java)
        }
    }
}