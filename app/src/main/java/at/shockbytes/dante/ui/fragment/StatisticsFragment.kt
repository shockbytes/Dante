package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.FragmentStatisticsBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.stats.StatsAdapter
import at.shockbytes.dante.ui.adapter.stats.model.ReadingGoalType
import at.shockbytes.dante.ui.viewmodel.StatisticsViewModel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOf

import javax.inject.Inject

class StatisticsFragment : BaseFragment<FragmentStatisticsBinding>() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var viewModel: StatisticsViewModel

    private val statsAdapter: StatsAdapter by lazy {
        StatsAdapter(
                requireContext(),
                imageLoader,
                onChangeGoalActionListener = viewModel::requestPageGoalChangeAction
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }


    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentStatisticsBinding {
        return FragmentStatisticsBinding.inflate(inflater, root, attachToRoot)
    }

    override fun setupViews() {
        setupToolbar()

        vb.fragmentStatisticsRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statsAdapter
        }
    }

    private fun setupToolbar() {
        with(vb.toolbarStatistics) {
            danteToolbarTitle.setText(R.string.label_stats)
            danteToolbarBack.apply {
                setVisible(true)
                setOnClickListener {
                    activity?.onBackPressed()
                }
            }
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestStatistics()
        viewModel.getStatistics().observe(this, Observer(statsAdapter::updateData))

        viewModel.onPageGoalChangeRequest()
                .subscribe(::handlePageReadingGoalState)
                .addTo(compositeDisposable)
    }

    private fun handlePageReadingGoalState(state: StatisticsViewModel.ReadingGoalState) {

        val initialValue = when (state) {
            is StatisticsViewModel.ReadingGoalState.Present -> state.goal
            is StatisticsViewModel.ReadingGoalState.Absent -> state.defaultGoal
        }

        val fragment = ReadingGoalPickerFragment
                .newInstance(initialValue, state.goalType)
                .setOnReadingGoalPickedListener(object : ReadingGoalPickerFragment.OnReadingGoalPickedListener {
                    override fun onGoalPicked(goal: Int, goalType: ReadingGoalType) {
                        viewModel.onGoalPicked(goal, goalType)
                    }

                    override fun onDelete(goalType: ReadingGoalType) {
                        viewModel.onGoalDeleted(goalType)
                    }
                })
        DanteUtils.addFragmentToActivity(parentFragmentManager, fragment, android.R.id.content, true)
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