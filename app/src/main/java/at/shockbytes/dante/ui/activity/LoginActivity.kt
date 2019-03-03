package at.shockbytes.dante.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.activity.core.BaseActivity
import at.shockbytes.dante.ui.viewmodel.LoginViewModel
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[LoginViewModel::class.java]
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }
}