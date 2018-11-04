package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import at.shockbytes.dante.R
import at.shockbytes.dante.book.BookStatistics
import at.shockbytes.dante.dagger.AppComponent

import at.shockbytes.dante.ui.viewmodel.StatisticsViewModel
import javax.inject.Inject

class StatisticsFragment: BaseFragment() {

    override val layoutId = R.layout.fragment_statistics

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[StatisticsViewModel::class.java]
    }

    override fun setupViews() {
        // Setup views...
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.statistics.observe(this, Observer { statistics ->
            statistics?.let {
                showStatistics(statistics)
            }
        })
        viewModel.requestStatistics()
    }

    override fun unbindViewModel() {
    }

    private fun showStatistics(statistics: BookStatistics) {
        showToast("Show statistics...")
    }

    companion object {

        fun newInstance(): StatisticsFragment {
            return StatisticsFragment()
        }
    }

}