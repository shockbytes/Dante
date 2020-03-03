package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.ui.adapter.BackupEntryAdapter
import at.shockbytes.dante.ui.adapter.OnBackupOverflowItemListener
import at.shockbytes.dante.ui.fragment.dialog.InactiveResourceDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.RestoreStrategyDialogFragment
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.isPortrait
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOfActivity
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.view.EqualSpaceItemDecoration
import com.google.android.gms.common.api.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_backup_restore.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.05.2019
 */
class BackupRestoreFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BackupMetadataState> {

    override val layoutId: Int = R.layout.fragment_backup_restore

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    private val entryAdapter: BackupEntryAdapter by lazy {
        BackupEntryAdapter(
            requireContext(),
            onItemClickListener = this,
            onItemOverflowMenuClickedListener = object: OnBackupOverflowItemListener {
                override fun onBackupItemDeleted(content: BackupMetadata, location: Int) {
                    onItemDismissed(content, location)
                }

                override fun onBackupItemDownloadRequest(content: BackupMetadata) {
                    // TODO
                    Timber.d(content.fileName)
                }
            }
        )
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(requireActivity(), vmFactory)
    }

    override fun setupViews() {
        rv_fragment_backup_restore.apply {
            layoutManager = getLayoutManagerForAdapter()
            adapter = entryAdapter
            addItemDecoration(EqualSpaceItemDecoration(16))
        }
    }

    private fun getLayoutManagerForAdapter(): RecyclerView.LayoutManager {
        return if (isPortrait()) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }
    }

    override fun onItemClick(content: BackupMetadataState, position: Int, v: View) {

        when (content) {
            is BackupMetadataState.Active -> showBackupRestoreStrategyModal(content)
            is BackupMetadataState.Inactive -> showInactiveResourceModal(content)
        }
    }

    private fun showInactiveResourceModal(t: BackupMetadataState.Inactive) {
        InactiveResourceDialogFragment
            .newInstance(t.entry.storageProvider)
            .show(childFragmentManager, "inactive-resource-dialog-fragment")
    }

    override fun bindViewModel() {

        viewModel.getBackupState().observe(this, Observer { state ->

            when (state) {
                is BackupViewModel.LoadBackupState.Success -> {
                    entryAdapter.updateData(state.backups)
                    rv_fragment_backup_restore.scrollToPosition(0)

                    showLoadingView(false)
                    showEmptyStateView(false)
                    showRecyclerView(true)
                }
                is BackupViewModel.LoadBackupState.Empty -> {
                    showLoadingView(false)
                    showEmptyStateView(true)
                    showRecyclerView(false)
                }
                is BackupViewModel.LoadBackupState.Loading -> {
                    showLoadingView(true)
                    showEmptyStateView(false)
                    showRecyclerView(false)
                }
                is BackupViewModel.LoadBackupState.Error -> {
                    showSnackbar(state.throwable.localizedMessage ?: "", showLong = true)
                    showLoadingView(false)
                    showEmptyStateView(false)
                    showRecyclerView(false)
                }
            }
        })

        viewModel.applyBackupEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                when (state) {
                    is BackupViewModel.ApplyBackupState.Success -> {
                        showSnackbar(getString(R.string.backup_restored, state.msg))
                    }
                    is BackupViewModel.ApplyBackupState.Error -> {
                        showSnackbar(getString(R.string.backup_restore_error, state.throwable.localizedMessage), showLong = true)
                    }
                }
            }
            .addTo(compositeDisposable)

        viewModel.deleteBackupEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { state ->
                when (state) {
                    is BackupViewModel.DeleteBackupState.Success -> {
                        val adapter = rv_fragment_backup_restore.adapter as BackupEntryAdapter
                        adapter.deleteEntity(state.deleteIndex)
                        showSnackbar(getString(R.string.backup_removed))

                        showEmptyStateView(state.isBackupListEmpty)
                    }
                    is BackupViewModel.DeleteBackupState.Error -> {
                        showSnackbar(getErrorMessage(state.throwable))
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    override fun unbindViewModel() {
    }

    private fun onItemDismissed(t: BackupMetadata, position: Int) {
        val currentItems = rv_fragment_backup_restore.adapter?.itemCount ?: -1
        viewModel.deleteItem(t, position, currentItems)
    }

    private fun showLoadingView(show: Boolean) {
        pb_fragment_backup_restore.setVisible(show)
    }

    private fun showEmptyStateView(show: Boolean) {
        view_fragment_backup_restore_empty.setVisible(show)
    }

    private fun showRecyclerView(show: Boolean) {
        rv_fragment_backup_restore.setVisible(show)
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> getString(R.string.error_msg_execution_exception)
            else -> getString(R.string.error_msg_unknown)
        }
    }

    private fun showBackupRestoreStrategyModal(state: BackupMetadataState.Active) {
        RestoreStrategyDialogFragment
            .newInstance()
            .setOnRestoreStrategySelectedListener { strategy ->
                viewModel.applyBackup(state.entry, strategy)
            }
            .show(childFragmentManager, "restore-strategy-dialog-fragment")
    }

    companion object {

        fun newInstance(): BackupRestoreFragment {
            return BackupRestoreFragment()
        }
    }
}