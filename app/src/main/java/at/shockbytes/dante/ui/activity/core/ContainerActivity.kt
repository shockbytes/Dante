package at.shockbytes.dante.ui.activity.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import at.shockbytes.dante.injection.AppComponent

abstract class ContainerActivity<B: ViewBinding> : BaseActivity<B>() {

    abstract val displayFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, displayFragment)
                .commit()
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit
}