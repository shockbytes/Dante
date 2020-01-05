package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.viewmodel.TimelineViewModel
import at.shockbytes.dante.util.viewModelOf
import javax.inject.Inject

class TimeLineFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_timeline

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TimelineViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModelOf(vmFactory)
        viewModel.requestTimeline()
    }

    override fun setupViews() {
        // TODO
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.getTimeLineState().observe(this, Observer { state ->
            handleTimeLineState(state)
        })
    }

    private fun handleTimeLineState(state: TimelineViewModel.TimeLineState) {
        // TODO
    }

    override fun unbindViewModel() {
        viewModel.getTimeLineState().removeObservers(this)
    }

    companion object {

        fun newInstance(): TimeLineFragment {
            return TimeLineFragment()
        }
    }
}