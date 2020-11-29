package at.shockbytes.dante.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import at.shockbytes.dante.ui.activity.core.ContainerActivity
import at.shockbytes.dante.ui.fragment.InspirationsFragment

class InspirationsActivity: ContainerActivity() {

    override val displayFragment: Fragment = InspirationsFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, InspirationsActivity::class.java)
    }
}