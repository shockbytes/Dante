package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.stats.StatsAdapter
import at.shockbytes.dante.ui.viewmodel.StatisticsViewModel
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_statistics.*
import javax.inject.Inject

class StatisticsFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_statistics

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: StatisticsViewModel

    private val statsAdapter: StatsAdapter by lazy {
        StatsAdapter(
                requireContext(),
                imageLoader,
                onChangeGoalActionListener = {
                    showToast("Change goal!")
                }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun setupViews() {
        fragment_statistics_rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statsAdapter
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestStatistics()
        viewModel.getStatistics().observe(this, Observer(statsAdapter::updateData))
    }

    override fun unbindViewModel() {
        viewModel.getStatistics().removeObservers(this)
    }

    companion object {

        fun newInstance(): StatisticsFragment {
            return StatisticsFragment()
        }
    }
}