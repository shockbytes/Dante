package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.StatisticsAdapter

import at.shockbytes.dante.ui.viewmodel.StatisticsViewModel
import at.shockbytes.util.view.EqualSpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_statistics.*
import javax.inject.Inject

class StatisticsFragment : BaseFragment() {

    override val layoutId = R.layout.fragment_statistics

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[StatisticsViewModel::class.java]
    }

    override fun setupViews() {
        fragment_statistics_rv.apply {
            val margin = requireContext().resources.getDimension(R.dimen.dimen_statistics_margin).toInt()
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(EqualSpaceItemDecoration(margin))
            adapter = StatisticsAdapter(requireContext())
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getStatistics().observe(this, Observer { items ->
            (fragment_statistics_rv.adapter as StatisticsAdapter).updateData(items)
        })
        viewModel.requestStatistics()
    }

    override fun unbindViewModel() {
    }

    companion object {

        fun newInstance(): StatisticsFragment {
            return StatisticsFragment()
        }
    }
}