package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.model.BackupMetadata
import at.shockbytes.dante.backup.model.BackupMetadataState
import at.shockbytes.dante.backup.model.BackupStorageProvider
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.BackupEntryAdapter
import at.shockbytes.dante.ui.fragment.dialog.RestoreStrategyDialogFragment
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.view.EqualSpaceItemDecoration
import com.google.android.gms.common.api.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_backup_legacy.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    31.12.2017
 */
class LegacyBackupFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BackupMetadataState> {

    override val layoutId = R.layout.fragment_backup_legacy

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
        context?.let { ctx ->
            val adapter = BackupEntryAdapter(ctx)
            fragment_backup_rv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(ctx)
            adapter.onItemClickListener = this
            adapter.onItemDeleteClickListener = { entry, position -> onItemDismissed(entry, position) }
            fragment_backup_rv.adapter = adapter
            fragment_backup_rv.addItemDecoration(EqualSpaceItemDecoration(8))

            fragment_backup_btn_backup.setOnClickListener {
                viewModel.makeBackup(BackupStorageProvider.GOOGLE_DRIVE) // TODO Replace this with more storage providers
            }
            fragment_backup_reload.setOnClickListener {
                viewModel.connect(requireActivity(), forceReload = false)
            }
        }
    }

    override fun onItemClick(t: BackupMetadataState, v: View) {

        when (t) {

            is BackupMetadataState.Active -> showBackupRestoreStrategyModal(t)

            is BackupMetadataState.Inactive -> {
                // TODO display something nicer here!
                showToast("Inactive resource...")
            }
        }
    }

    private fun showBackupRestoreStrategyModal(state: BackupMetadataState.Active) {
        RestoreStrategyDialogFragment
            .newInstance()
            .setOnRestoreStrategySelectedListener { strategy ->
                viewModel.applyBackup(state.entry, strategy)
            }
            .show(fragmentManager, "restore-strategy-dialog-fragment")
    }

    override fun bindViewModel() {

        viewModel.getBackupState().observe(this, Observer { state ->

            when (state) {
                is BackupViewModel.LoadBackupState.Success -> {
                    val backupList = state.backups.toMutableList()
                    (fragment_backup_rv.adapter as BackupEntryAdapter).data = backupList
                    fragment_backup_rv.scrollToPosition(0)
                    fragment_backup_txt_restore.text = getString(R.string.restore, backupList.size)

                    showLoadingView(false)
                    showEmptyStateView(false)
                    showRecyclerView(true)
                }
                is BackupViewModel.LoadBackupState.Empty -> {
                    showLoadingView(false)
                    showEmptyStateView(true)
                    showRecyclerView(false)

                    fragment_backup_txt_restore.text = getString(R.string.restore, 0)
                }
                is BackupViewModel.LoadBackupState.Loading -> {
                    showLoadingView(true)
                    showEmptyStateView(false)
                    showRecyclerView(false)
                }
                is BackupViewModel.LoadBackupState.Error -> {
                    showSnackbar(state.throwable.localizedMessage, showLong = true)
                    showLoadingView(false)
                    showEmptyStateView(false)
                    showRecyclerView(false)
                }
            }
        })

        viewModel.getLastBackupTime().observe(this, Observer { lastBackup ->
            fragment_backup_txt_last_backup.text = getString(R.string.last_backup, lastBackup)
        })

        viewModel.makeBackupEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    when (state) {
                        is BackupViewModel.State.Success -> {
                            showToast(getString(R.string.backup_created), showLong = false)
                        }
                        is BackupViewModel.State.Error -> {
                            showSnackbar(getString(R.string.backup_not_created))
                        }
                    }
                }
                .addTo(compositeDisposable)

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
                            val adapter = fragment_backup_rv.adapter as BackupEntryAdapter
                            adapter.deleteEntity(state.deleteIndex)
                            showSnackbar(getString(R.string.backup_removed))
                            fragment_backup_txt_restore.text = getString(R.string.restore, adapter.itemCount)

                            showEmptyStateView(state.isBackupListEmpty)
                        }
                        is BackupViewModel.DeleteBackupState.Error -> {
                            showSnackbar(getErrorMessage(state.throwable))
                        }
                    }
                }
                .addTo(compositeDisposable)

        viewModel.errorSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ error ->
                    showToast(getString(R.string.backup_connection_establish_error, getErrorMessage(error)), showLong = true)
                }, { throwable ->
                    Timber.e(throwable)
                })
                .addTo(compositeDisposable)

        viewModel.connect(requireActivity(), forceReload = false)
    }

    override fun unbindViewModel() {
        viewModel.disconnect()
    }

    private fun onItemDismissed(t: BackupMetadata, position: Int) {
        val currentItems = fragment_backup_rv.adapter?.itemCount ?: -1
        viewModel.deleteItem(t, position, currentItems)
    }

    private fun showLoadingView(show: Boolean) {
        fragment_backup_pb.setVisible(show)
    }

    private fun showEmptyStateView(show: Boolean) {
        fragment_backup_empty_view.setVisible(show)
    }

    private fun showRecyclerView(show: Boolean) {
        fragment_backup_rv.setVisible(show)
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> getString(R.string.error_msg_execution_exception)
            else -> getString(R.string.error_msg_unknown)
        }
    }

    companion object {

        fun newInstance(): LegacyBackupFragment {
            return LegacyBackupFragment().apply {
                this.arguments = Bundle().apply {
                }
            }
        }
    }
}