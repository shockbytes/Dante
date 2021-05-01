package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.FragmentFeatureFlagConfigBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.FeatureFlagConfigAdapter
import at.shockbytes.dante.ui.viewmodel.FeatureFlagConfigViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class FeatureFlagConfigFragment : BaseFragment<FragmentFeatureFlagConfigBinding>() {

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

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentFeatureFlagConfigBinding {
        return FragmentFeatureFlagConfigBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        vb.layoutFeatureFlagConfig.setOnClickListener {
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
        vb.rvFeatureFlags.apply {
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