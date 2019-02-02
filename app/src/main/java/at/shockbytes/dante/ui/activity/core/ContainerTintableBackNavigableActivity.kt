package at.shockbytes.dante.ui.activity.core

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * @author Martin Macheiner
 * Date: 23.12.2017.
 */
abstract class ContainerTintableBackNavigableActivity : TintableBackNavigableActivity() {

    abstract val displayFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .commit()
    }
}