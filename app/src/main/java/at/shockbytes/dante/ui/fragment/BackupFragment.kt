package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.BackupPagerAdapter
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import kotlinx.android.synthetic.main.fragment_backup.*
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_backup

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)[BackupViewModel::class.java]
    }

    override fun setupViews() {
        setupViewPager()

        tabs_fragment_backup.setupWithViewPager(vp_fragment_backup)
    }

    override fun bindViewModel() {
        activity?.let { act ->
            viewModel.connect(act)
        }
    }

    override fun unbindViewModel() {
        viewModel.disconnect()
    }

    private fun setupViewPager() {
        val pagerAdapter = BackupPagerAdapter(requireContext(), childFragmentManager)

        vp_fragment_backup.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2
        }
    }

    companion object {

        fun newInstance(): BackupFragment {
            return BackupFragment().apply {
                this.arguments = Bundle().apply {
                }
            }
        }
    }
}