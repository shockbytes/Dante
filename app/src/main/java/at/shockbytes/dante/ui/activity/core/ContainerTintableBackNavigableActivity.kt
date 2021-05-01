package at.shockbytes.dante.ui.activity.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Author:  Martin Macheiner
 * Date:    23.12.2017
 */
abstract class ContainerTintableBackNavigableActivity<V : ViewBinding> : TintableBackNavigableActivity<V>() {

    abstract val displayFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, displayFragment)
            .commit()
    }
}