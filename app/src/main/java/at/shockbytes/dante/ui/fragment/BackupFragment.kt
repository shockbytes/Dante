package at.shockbytes.dante.ui.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import at.shockbytes.dante.R
import at.shockbytes.dante.adapter.BackupEntryAdapter
import at.shockbytes.dante.backup.BackupEntry
import at.shockbytes.dante.backup.BackupManager
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.fragment.dialogs.RestoreStrategyDialogFragment
import at.shockbytes.dante.util.ResourceManager
import at.shockbytes.dante.util.books.BookManager
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import at.shockbytes.util.view.EqualSpaceItemDecoration
import butterknife.OnClick
import kotterknife.bindView
import java.util.*
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 31.12.2017.
 */

class BackupFragment : BaseFragment(), BaseAdapter.OnItemClickListener<BackupEntry>,
        CompoundButton.OnCheckedChangeListener, BaseAdapter.OnItemMoveListener<BackupEntry> {

    interface OnBackupRestoreListener {

        fun onBackupRestored()
    }

    override val layoutId = R.layout.activity_backup

    private val rvBackups: RecyclerView by bindView(R.id.activity_backup_rv_backups)
    private val switchAutoUpdate: Switch by bindView(R.id.activity_backup_switch_auto_update)
    private val txtLastBackup: TextView by bindView(R.id.activity_backup_txt_last_backup)
    private val btnBackup: AppCompatButton by bindView(R.id.activity_backup_btn_backup)

    @Inject
    protected lateinit var bookManager: BookManager

    @Inject
    protected lateinit var backupManager: BackupManager

    @Inject
    protected lateinit var tracker: Tracker

    private lateinit var adapter: BackupEntryAdapter

    private var backupRestoreListener: OnBackupRestoreListener? = null

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        backupRestoreListener = context as? OnBackupRestoreListener
    }

    @OnClick(R.id.activity_backup_btn_backup)
    protected fun onClickBackup() {

        backupManager.backup(bookManager.allBooksSync).subscribe({
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
        setStateOfBackupButton(backupManager.isAutoBackupEnabled)

        switchAutoUpdate.isChecked = backupManager.isAutoBackupEnabled
        switchAutoUpdate.setOnCheckedChangeListener(this)

        adapter = BackupEntryAdapter(context, ArrayList())
        rvBackups.layoutManager = LinearLayoutManager(context)
        adapter.onItemClickListener = this
        adapter.onItemMoveListener = this
        val callback = BaseItemTouchHelper(adapter, true,
                BaseItemTouchHelper.DragAccess.NONE)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rvBackups)
        rvBackups.adapter = adapter
        rvBackups.addItemDecoration(EqualSpaceItemDecoration(8))

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
                                        ResourceManager.formatTimestamp(t.timestamp)))
                            }) { throwable ->
                                throwable.printStackTrace()
                                showSnackbar(getString(R.string.backup_restore_error))
                            }
                }
                .show(fragmentManager, "restore-strategy-dialog-fragment")
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        backupManager.isAutoBackupEnabled = b
        setStateOfBackupButton(b)
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
        }) { throwable ->
            throwable.printStackTrace()
            Toast.makeText(context, throwable.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateLastBackupTime() {
        val lastBackupMillis = backupManager.lastBackupTime
        val lastBackup = if (lastBackupMillis > 0)
            ResourceManager.formatTimestamp(lastBackupMillis)
        else
            "---"
        txtLastBackup.text = getString(R.string.last_backup, lastBackup)
    }

    private fun setStateOfBackupButton(b: Boolean) {
        btnBackup.isEnabled = !b
        val colorID = if (b) R.color.disabled_view else R.color.colorAccent
        btnBackup.supportBackgroundTintList = ColorStateList
                .valueOf(ContextCompat.getColor(context, colorID))
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