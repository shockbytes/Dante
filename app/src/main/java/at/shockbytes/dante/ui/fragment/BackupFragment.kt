package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import at.shockbytes.dante.R
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.BackupPagerAdapter
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.addTo
import com.google.android.gms.common.api.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_backup.*
import timber.log.Timber
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

        viewModel.errorSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ error ->
                showToast(getString(R.string.backup_connection_establish_error, getErrorMessage(error)), showLong = true)
            }, { throwable ->
                Timber.e(throwable)
            })
            .addTo(compositeDisposable)

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

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> getString(R.string.error_msg_execution_exception)
            else -> throwable.localizedMessage
        }
    }

    fun switchToBackupTab() {
        vp_fragment_backup.setCurrentItem(1, true)
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