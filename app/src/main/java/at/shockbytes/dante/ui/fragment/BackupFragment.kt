package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BackupEntryAdapter
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.books.BookManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.fragment.dialog.RestoreStrategyDialogFragment
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import at.shockbytes.util.view.EqualSpaceItemDecoration
import kotlinx.android.synthetic.main.activity_backup.*
import kotterknifex.bindView
import java.util.*
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 31.12.2017.
 */

class BackupFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BackupEntry>,
        BaseAdapter.OnItemMoveListener<BackupEntry> {

    interface OnBackupRestoreListener {

        fun onBackupRestored()
    }

    @Inject
    protected lateinit var bookManager: BookManager

    @Inject
    protected lateinit var backupManager: BackupManager

    @Inject
    protected lateinit var tracker: Tracker

    private lateinit var adapter: BackupEntryAdapter

    private var backupRestoreListener: OnBackupRestoreListener? = null

    private val rvBackups: RecyclerView by bindView(R.id.activity_backup_rv_backups)
    private val txtLastBackup: TextView by bindView(R.id.activity_backup_txt_last_backup)

    override val layoutId = R.layout.activity_backup

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        backupRestoreListener = context as? OnBackupRestoreListener
    }

    override fun setupViews() {

        updateLastBackupTime()

        adapter = BackupEntryAdapter(context!!, ArrayList())
        rvBackups.layoutManager = LinearLayoutManager(context!!)
        adapter.onItemClickListener = this
        adapter.onItemMoveListener = this
        val callback = BaseItemTouchHelper(adapter, true,
                BaseItemTouchHelper.DragAccess.NONE)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rvBackups)
        rvBackups.adapter = adapter
        rvBackups.addItemDecoration(EqualSpaceItemDecoration(8))

        btnActivityBackupBackup.setOnClickListener {
            backup()
        }

        loadBackupList()
    }

    override fun onItemClick(t: BackupEntry, v: View) {
        RestoreStrategyDialogFragment.newInstance()
                .setOnRestoreStrategySelectedListener { strategy ->
                    backupManager.restoreBackup(t, bookManager, strategy)
                            .subscribe({
                                tracker.trackOnBackupRestored()
                                backupRestoreListener?.onBackupRestored() // Notify MainActivity
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
                }) { throwable ->
                    throwable.printStackTrace()
                    showSnackbar(throwable.localizedMessage)
                }
    }

    private fun loadBackupList() {
        backupManager.backupList.subscribe({ backupEntries ->
            adapter.data = backupEntries.toMutableList()
            rvBackups.scrollToPosition(0)
        }) { throwable ->
            throwable.printStackTrace()
            Toast.makeText(context, throwable.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun backup() {
        backupManager.backup(bookManager.allBooks).subscribe({
            showSnackbar(getString(R.string.backup_created))
            updateLastBackupTime()
            loadBackupList()
            tracker.trackOnBackupMade()
        }) { throwable ->
            throwable.printStackTrace()
            showSnackbar(getString(R.string.backup_not_created))
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