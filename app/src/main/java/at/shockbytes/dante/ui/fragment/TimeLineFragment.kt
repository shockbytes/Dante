package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.timeline.TimeLineItem
import at.shockbytes.dante.ui.adapter.timeline.TimeLineAdapter
import at.shockbytes.dante.ui.viewmodel.TimelineViewModel
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.fragment_timeline.*
import javax.inject.Inject

class TimeLineFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_timeline

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TimelineViewModel

    private val timeLineAdapter: TimeLineAdapter by lazy {
        TimeLineAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = viewModelOf(vmFactory)
        viewModel.requestTimeline()
    }

    override fun setupViews() {
        rv_timeline.adapter = timeLineAdapter
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
        when (state) {
            TimelineViewModel.TimeLineState.Loading -> handleLoadingState()
            TimelineViewModel.TimeLineState.Error -> handleErrorState()
            TimelineViewModel.TimeLineState.Empty -> handleEmptyState()
            is TimelineViewModel.TimeLineState.Success -> handleSuccessState(state.content)
        }
    }

    private fun handleLoadingState() {
        rv_timeline.setVisible(false)
        layout_timeline_error.setVisible(false)
        layout_timeline_empty.setVisible(false)
        pb_timeline_loading.setVisible(true)
    }

    private fun handleErrorState() {
        rv_timeline.setVisible(false)
        layout_timeline_empty.setVisible(false)
        pb_timeline_loading.setVisible(false)
        layout_timeline_error.setVisible(true)
    }

    private fun handleEmptyState() {
        rv_timeline.setVisible(false)
        layout_timeline_error.setVisible(false)
        layout_timeline_empty.setVisible(true)
        pb_timeline_loading.setVisible(false)
    }

    private fun handleSuccessState(content: List<TimeLineItem>) {
        layout_timeline_error.setVisible(false)
        layout_timeline_empty.setVisible(false)
        pb_timeline_loading.setVisible(false)

        rv_timeline.setVisible(true)

        timeLineAdapter.data.apply {
            clear()
            addAll(content)
        }
        timeLineAdapter.notifyDataSetChanged()
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