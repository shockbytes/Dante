package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.StatisticsAdapter

import at.shockbytes.dante.ui.viewmodel.StatisticsViewModel
import at.shockbytes.util.view.EqualSpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_statistics.*
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
        context?.let { ctx ->
            fragment_statistics_rv.layoutManager = LinearLayoutManager(ctx)
            val margin = ctx.resources.getDimension(R.dimen.dimen_statistics_margin).toInt()
            fragment_statistics_rv.addItemDecoration(EqualSpaceItemDecoration(margin))
            fragment_statistics_rv.adapter = StatisticsAdapter(ctx)
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getStatistics().observe(this, Observer { items ->
            items?.toMutableList()?.let {
                (fragment_statistics_rv.adapter as StatisticsAdapter).data = it
            }
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