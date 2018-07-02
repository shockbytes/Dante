package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.data.BookEntityDao
import at.shockbytes.dante.ui.adapter.BackupEntryAdapter
import at.shockbytes.dante.ui.fragment.dialog.RestoreStrategyDialogFragment
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import at.shockbytes.util.view.EqualSpaceItemDecoration
import kotlinx.android.synthetic.main.activity_backup.*
import kotterknife.bindView
import java.util.*
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 31.12.2017.
 */

class BackupFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BackupEntry>,
        BaseAdapter.OnItemMoveListener<BackupEntry> {

    @Inject
    protected lateinit var bookDao: BookEntityDao

    @Inject
    protected lateinit var backupManager: BackupManager

    @Inject
    protected lateinit var tracker: Tracker

    private lateinit var adapter: BackupEntryAdapter

    private val rvBackups: RecyclerView by bindView(R.id.activity_backup_rv_backups)
    private val txtLastBackup: TextView by bindView(R.id.activity_backup_txt_last_backup)

    override val layoutId = R.layout.activity_backup

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    protected fun onClickBackup() {
        backupManager.backup(bookDao.bookObservable).subscribe({
            showSnackbar(getString(R.string.backup_created))
            updateLastBackupTime()
            loadBackupList()
            tracker.trackOnBackupMade()
        }) { throwable ->
            throwable.printStackTrace()
            showSnackbar(getString(R.string.backup_not_created))
        }
    }

    override fun setupViews() {

        updateLastBackupTime()

        context?.let { ctx ->

            adapter = BackupEntryAdapter(ctx, ArrayList())
            rvBackups.layoutManager = LinearLayoutManager(ctx)
            adapter.onItemClickListener = this
            adapter.onItemMoveListener = this
            val callback = BaseItemTouchHelper(adapter, true,
                    BaseItemTouchHelper.DragAccess.NONE)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(rvBackups)
            rvBackups.adapter = adapter
            rvBackups.addItemDecoration(EqualSpaceItemDecoration(8))

            activity_backup_btn_backup.setOnClickListener {
                onClickBackup()
            }

            loadBackupList()
        }

    }

    override fun onItemClick(t: BackupEntry, v: View) {
        RestoreStrategyDialogFragment.newInstance()
                .setOnRestoreStrategySelectedListener { strategy ->
                    backupManager.restoreBackup(t, bookDao, strategy)
                            .subscribe({
                                tracker.trackOnBackupRestored()
                                showSnackbar(getString(R.string.backup_restored,
                                        DanteUtils.formatTimestamp(t.timestamp)))
                            }) { throwable ->
                                throwable.printStackTrace()
                                showSnackbar(getString(R.string.backup_restore_error))
                            }
                }
                .show(fragmentManager, "restore-strategy-dialog-fragment")
    }

    override fun onItemMove(t: BackupEntry, from: Int, to: Int) {}

    override fun onItemMoveFinished() {}

    override fun onItemDismissed(t: BackupEntry, position: Int) {
        backupManager.removeBackupEntry(t)
                .subscribe({
                    adapter.deleteEntity(position)
                    showSnackbar(getString(R.string.backup_removed))
                    activityBackupTxtRestore.text = "${getString(R.string.restore)} (${adapter.itemCount})"
                }) { throwable ->
                    throwable.printStackTrace()
                    showSnackbar(throwable.localizedMessage)
                }
    }

    private fun loadBackupList() {
        backupManager.backupList.subscribe({ backupEntries ->
            adapter.data = backupEntries.toMutableList()
            rvBackups.scrollToPosition(0)
            activityBackupTxtRestore.text = "${getString(R.string.restore)} (${backupEntries.size})"
        }) { throwable ->
            throwable.printStackTrace()
            Toast.makeText(context, throwable.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateLastBackupTime() {
        val lastBackupMillis = backupManager.lastBackupTime
        val lastBackup = if (lastBackupMillis > 0)
            DanteUtils.formatTimestamp(lastBackupMillis)
        else
            "---"
        txtLastBackup.text = getString(R.string.last_backup, lastBackup)
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