package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import at.shockbytes.dante.ui.activity.core.ContainerActivity
import at.shockbytes.dante.ui.fragment.StatisticsFragment

class StatisticsActivity : ContainerActivity() {

    override val displayFragment = StatisticsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, StatisticsActivity::class.java)
    }
}