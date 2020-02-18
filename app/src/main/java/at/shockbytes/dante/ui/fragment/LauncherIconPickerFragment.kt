package at.shockbytes.dante.ui.fragment

import `in`.myinnos.library.AppIconNameChanger
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.LauncherIconPickerAdapter
import at.shockbytes.dante.ui.viewmodel.LauncherIconPickerViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOf
import at.shockbytes.util.adapter.BaseAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_launcher_icon_picker.*
import timber.log.Timber
import javax.inject.Inject

class LauncherIconPickerFragment: BaseFragment() {

    override val layoutId: Int = R.layout.fragment_launcher_icon_picker

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LauncherIconPickerViewModel

    private val launcherItemAdapter: LauncherIconPickerAdapter by lazy {
        LauncherIconPickerAdapter(
            requireContext(),
            object : BaseAdapter.OnItemClickListener<LauncherIconPickerViewModel.LauncherIconItem> {
                override fun onItemClick(content: LauncherIconPickerViewModel.LauncherIconItem, position: Int, v: View) {
                    viewModel.applyLauncher(content.iconLauncherIconState)
                }

            }
        )
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
        viewModel.requestLauncherItems()
        viewModel.getLauncherItems().observe(this, Observer(launcherItemAdapter::updateData))

        viewModel.onApplyLauncherEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                showToast(R.string.restarting_app)
                parentFragmentManager.popBackStack()
            }
            .subscribe({ (activeName, disableNames) ->
                applyLauncherChange(activeName, disableNames)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)
    }

    private fun applyLauncherChange(activeName: String, disableNames: List<String>) {
        AppIconNameChanger.Builder(requireActivity())
            .activeName(activeName)
            .disableNames(disableNames)
            .packageName(BuildConfig.APPLICATION_ID)
            .build()
            .setNow()
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