package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.FeatureFlagConfigAdapter
import at.shockbytes.dante.ui.viewmodel.FeatureFlagConfigViewModel
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_feature_flag_config.*
import javax.inject.Inject

class FeatureFlagConfigFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_feature_flag_config

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: FeatureFlagConfigViewModel

    private val flagAdapter: FeatureFlagConfigAdapter by lazy {
        FeatureFlagConfigAdapter(requireContext()) { item ->
            viewModel.updateFeatureFlag(item.key, item.value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
        layout_feature_flag_config.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        setupRecyclerView()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getFeatureFlagItems().observe(this, Observer { listItems ->
            flagAdapter.data = listItems.toMutableList()
        })
    }

    override fun unbindViewModel() {
        viewModel.getFeatureFlagItems().removeObservers(this)
    }

    private fun setupRecyclerView() {
        rv_feature_flags.apply {
            adapter = flagAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    companion object {

        fun newInstance(): FeatureFlagConfigFragment {
            return FeatureFlagConfigFragment()
        }
    }
}