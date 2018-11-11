package at.shockbytes.dante.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.adapter.BackupEntryAdapter
import at.shockbytes.dante.ui.fragment.dialog.RestoreStrategyDialogFragment
import at.shockbytes.dante.ui.viewmodel.BackupViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.setVisible
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import at.shockbytes.util.view.EqualSpaceItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_backup.*
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    31.12.2017
 */
class BackupFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BackupEntry>,
        BaseAdapter.OnItemMoveListener<BackupEntry> {

    override val layoutId = R.layout.fragment_backup

    @Inject
    protected lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BackupViewModel

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory)[BackupViewModel::class.java]
    }

    override fun setupViews() {
        context?.let { ctx ->
            val adapter = BackupEntryAdapter(ctx, listOf())
            fragment_backup_rv.layoutManager = LinearLayoutManager(ctx)
            adapter.onItemClickListener = this
            adapter.onItemMoveListener = this
            val callback = BaseItemTouchHelper(adapter, true,
                    BaseItemTouchHelper.DragAccess.NONE)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(fragment_backup_rv)
            fragment_backup_rv.adapter = adapter
            fragment_backup_rv.addItemDecoration(EqualSpaceItemDecoration(8))

            fragment_backup_btn_backup.setOnClickListener {
                viewModel.makeBackup()
            }
        }
    }

    override fun onItemClick(t: BackupEntry, v: View) {
        RestoreStrategyDialogFragment.newInstance()
                .setOnRestoreStrategySelectedListener { strategy ->
                    viewModel.applyBackup(t, strategy)
                }
                .show(fragmentManager, "restore-strategy-dialog-fragment")
    }

    override fun onItemMove(t: BackupEntry, from: Int, to: Int) {}

    override fun onItemMoveFinished() {}

    override fun onItemDismissed(t: BackupEntry, position: Int) {
        viewModel.deleteItem(t, position)
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
                    Toast.makeText(context, state.throwable.localizedMessage, Toast.LENGTH_LONG).show()
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
                            showSnackbar(getString(R.string.backup_created))
                        }
                        is BackupViewModel.State.Error -> {
                            showSnackbar(getString(R.string.backup_not_created))
                        }
                    }

                }.addTo(compositeDisposable)

        viewModel.applyBackupEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    when (state) {
                        is BackupViewModel.ApplyBackupState.Success -> {
                            showSnackbar(getString(R.string.backup_restored, state.msg))
                        }
                        is BackupViewModel.ApplyBackupState.Error -> {
                            showSnackbar(getString(R.string.backup_restore_error))
                        }
                    }

                }.addTo(compositeDisposable)

        viewModel.deleteBackupEvent
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state ->
                    when (state) {
                        is BackupViewModel.DeleteBackupState.Success -> {
                            val adapter = fragment_backup_rv.adapter as BackupEntryAdapter
                            adapter.deleteEntity(state.deleteIndex)
                            showSnackbar(getString(R.string.backup_removed))
                            fragment_backup_txt_restore.text = getString(R.string.restore, adapter.itemCount)
                        }
                        is BackupViewModel.DeleteBackupState.Error -> {
                            showSnackbar(state.throwable.localizedMessage)
                        }
                    }
                }.addTo(compositeDisposable)
    }

    override fun unbindViewModel() {
        // Not needed...
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

    companion object {

        fun newInstance(): BackupFragment {
            val fragment = BackupFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}