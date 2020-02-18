package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.LauncherIconPickerAdapter
import at.shockbytes.dante.ui.viewmodel.LauncherIconPickerViewModel
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_launcher_icon_picker.*
import javax.inject.Inject

class LauncherIconPickerFragment: BaseFragment() {

    override val layoutId: Int = R.layout.fragment_launcher_icon_picker

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LauncherIconPickerViewModel

    private val launcherItemAdapter: LauncherIconPickerAdapter by lazy {
        LauncherIconPickerAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
        layout_launcher_icon_items.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        setupRecyclerView()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getLauncherItems().observe(this, Observer(launcherItemAdapter::updateData))
    }

    override fun unbindViewModel() {
        viewModel.getLauncherItems().removeObservers(this)
    }

    private fun setupRecyclerView() {
        rv_launcher_icon_items.apply {
            adapter = launcherItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    companion object {

        fun newInstance(): LauncherIconPickerFragment {
            return LauncherIconPickerFragment()
        }
    }
}