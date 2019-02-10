package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.viewmodel.FeatureFlagConfigViewModel
import kotlinx.android.synthetic.main.fragment_feature_flag_config.*
import javax.inject.Inject

class FeatureFlagConfigFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_feature_flag_config

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: FeatureFlagConfigViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)[FeatureFlagConfigViewModel::class.java]
    }

    override fun setupViews() {
        layout_feature_flag_config.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): FeatureFlagConfigFragment {
            return FeatureFlagConfigFragment()
        }
    }
}