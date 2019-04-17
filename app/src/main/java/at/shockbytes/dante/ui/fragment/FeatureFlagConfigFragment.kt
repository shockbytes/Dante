package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.FeatureFlagConfigAdapter
import at.shockbytes.dante.ui.viewmodel.FeatureFlagConfigViewModel
import kotlinx.android.synthetic.main.fragment_feature_flag_config.*
import javax.inject.Inject

class FeatureFlagConfigFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_feature_flag_config

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: FeatureFlagConfigViewModel

    private val flagAdapter: FeatureFlagConfigAdapter by lazy {
        context?.let { ctx ->
            FeatureFlagConfigAdapter(ctx) { item ->
                viewModel.updateFeatureFlag(item.key, item.value)
            }
        } ?: throw IllegalStateException("Context must not be null when lazy loading FlagAdapter!")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)[FeatureFlagConfigViewModel::class.java]
    }

    override fun setupViews() {
        layout_feature_flag_config.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        setupRecyclerView()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getFeatureFlagItems().observe(this, Observer { listItems ->
            listItems?.let { items ->
                flagAdapter.data = items.toMutableList()
            }
        })
    }

    override fun unbindViewModel() {
        viewModel.getFeatureFlagItems().removeObservers(this)
    }

    private fun setupRecyclerView() {
        context?.let { ctx ->
            rv_feature_flags.apply {
                adapter = flagAdapter
                layoutManager = LinearLayoutManager(ctx)
                addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
            }
        }
    }

    companion object {

        fun newInstance(): FeatureFlagConfigFragment {
            return FeatureFlagConfigFragment()
        }
    }
}