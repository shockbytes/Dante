package at.shockbytes.dante.ui.activity.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import at.shockbytes.dante.dagger.AppComponent

/**
 * Author:  Martin Macheiner
 * Date:    23.12.2017
 */
abstract class ContainerBackNavigableActivity : BackNavigableActivity() {

    abstract val displayFragment: androidx.fragment.app.Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
}