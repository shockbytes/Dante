package at.shockbytes.dante.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.adapter.BackupEntryAdapter;
import at.shockbytes.dante.backup.BackupEntry;
import at.shockbytes.dante.backup.BackupManager;
import at.shockbytes.dante.dagger.AppComponent;
import at.shockbytes.dante.ui.fragment.dialogs.RestoreStrategyDialogFragment;
import at.shockbytes.dante.util.ResourceManager;
import at.shockbytes.dante.util.books.BookManager;
import at.shockbytes.dante.util.tracking.Tracker;
import at.shockbytes.util.adapter.BaseAdapter;
import at.shockbytes.util.adapter.BaseItemTouchHelper;
import at.shockbytes.util.view.EqualSpaceItemDecoration;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * TODO Candidate for ContainerBackNavigableActivity
 */
public class BackupActivity extends BackNavigableActivity
        implements BaseAdapter.OnItemClickListener<BackupEntry>,
        CompoundButton.OnCheckedChangeListener, BaseAdapter.OnItemMoveListener<BackupEntry> {

    public static Intent newIntent(Context context) {
        return new Intent(context, BackupActivity.class);
    }

    @BindView(R.id.activity_backup_root)
    protected RelativeLayout rootLayout;

    @BindView(R.id.activity_backup_rv_backups)
    protected RecyclerView rvBackups;

    @BindView(R.id.activity_backup_switch_auto_update)
    protected Switch switchAutoUpdate;

    @BindView(R.id.activity_backup_txt_last_backup)
    protected TextView txtLastBackup;

    @BindView(R.id.activity_backup_btn_backup)
    protected AppCompatButton btnBackup;

    @Inject
    protected BookManager bookManager;

    @Inject
    protected BackupManager backupManager;

    @Inject
    protected Tracker tracker;

    private BackupEntryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        setupViews();
    }

    @OnClick(R.id.activity_backup_btn_backup)
    protected void onClickBackup() {

        backupManager.backup(bookManager.getAllBooksSync()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                showSnackbar(getString(R.string.backup_created));
                updateLastBackupTime();
                loadBackupList();

                tracker.trackOnBackupMade();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
                showSnackbar(getString(R.string.backup_not_created));
            }
        });
    }

    private void setupViews() {

        updateLastBackupTime();
        setStateOfBackupButton(backupManager.isAutoBackupEnabled());

        switchAutoUpdate.setChecked(backupManager.isAutoBackupEnabled());
        switchAutoUpdate.setOnCheckedChangeListener(this);

        adapter = new BackupEntryAdapter(this, new ArrayList<BackupEntry>());
        rvBackups.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter.setOnItemClickListener(this);
        adapter.setOnItemMoveListener(this);
        ItemTouchHelper.Callback callback = new BaseItemTouchHelper(adapter, true,
                BaseItemTouchHelper.DragAccess.NONE);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rvBackups);
        rvBackups.setAdapter(adapter);
        rvBackups.addItemDecoration(new EqualSpaceItemDecoration(8));

        loadBackupList();
    }

    @Override
    public void onItemClick(final BackupEntry entry, View v) {

        RestoreStrategyDialogFragment rsdf = RestoreStrategyDialogFragment.newInstance();
        rsdf.setOnRestoreStrategySelectedListener(new RestoreStrategyDialogFragment.OnRestoreStrategySelectedListener() {
            @Override
            public void onRestoreStrategySelected(BackupManager.RestoreStrategy strategy) {

                backupManager.restoreBackup(BackupActivity.this, entry, bookManager, strategy)
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                tracker.trackOnBackupRestored();
                                showSnackbar(getString(R.string.backup_restored,
                                        ResourceManager.formatTimestamp(entry.getTimestamp())));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                showSnackbar(getString(R.string.backup_restore_error));
                            }
                        });
            }
        });
        rsdf.show(getSupportFragmentManager(), rsdf.getTag());
    }

    @Override
    public void injectToGraph(@NotNull AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        backupManager.setAutoBackupEnabled(b);
        setStateOfBackupButton(b);
    }

    @Override
    public void onItemMove(BackupEntry entry, int from, int to) {

    }

    @Override
    public void onItemMoveFinished() {

    }

    @Override
    public void onItemDismissed(final BackupEntry entry, final int position) {

        backupManager.removeBackupEntry(entry)
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        adapter.deleteEntity(position);
                        showSnackbar(getString(R.string.backup_removed));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        showSnackbar(throwable.getMessage());
                    }
                });
    }

    private void loadBackupList() {
        backupManager.getBackupList().subscribe(new Consumer<List<BackupEntry>>() {
            @Override
            public void accept(List<BackupEntry> backupEntries) {
                adapter.setData(backupEntries);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                throwable.printStackTrace();
                Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateLastBackupTime() {
        long lastBackupMillis = backupManager.getLastBackupTime();
        String lastBackup = (lastBackupMillis > 0)
                ? ResourceManager.formatTimestamp(lastBackupMillis)
                : "---";
        txtLastBackup.setText(getString(R.string.last_backup, lastBackup));
    }

    private void setStateOfBackupButton(boolean b) {
        btnBackup.setEnabled(!b);
        int colorID = b ? R.color.disabled_view : R.color.colorAccent;
        btnBackup.setSupportBackgroundTintList(ColorStateList
                .valueOf(ContextCompat.getColor(this, colorID)));
    }

}
