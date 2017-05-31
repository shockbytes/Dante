package at.shockbytes.dante.core;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.ResourceManager;
import at.shockbytes.dante.adapter.BackupEntryAdapter;
import at.shockbytes.dante.adapter.BaseAdapter;
import at.shockbytes.dante.adapter.helper.BackupEntryTouchHelper;
import at.shockbytes.dante.fragments.dialogs.RestoreStrategyDialogFragment;
import at.shockbytes.dante.util.backup.BackupEntry;
import at.shockbytes.dante.util.backup.BackupManager;
import at.shockbytes.dante.util.books.BookManager;
import at.shockbytes.util.view.EqualSpaceItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

public class BackupActivity extends AppCompatActivity
        implements BaseAdapter.OnItemClickListener<BackupEntry>,
        CompoundButton.OnCheckedChangeListener, BaseAdapter.OnItemMoveListener<BackupEntry> {

    public static Intent newIntent(Context context) {
        return new Intent(context, BackupActivity.class);
    }

    @Bind(R.id.activity_backup_root)
    protected RelativeLayout rootLayout;

    @Bind(R.id.activity_backup_rv_backups)
    protected RecyclerView rvBackups;

    @Bind(R.id.activity_backup_switch_auto_update)
    protected Switch switchAutoUpdate;

    @Bind(R.id.activity_backup_txt_last_backup)
    protected TextView txtLastBackup;

    @Bind(R.id.activity_backup_btn_backup)
    protected AppCompatButton btnBackup;

    @Inject
    protected BookManager bookManager;

    @Inject
    protected BackupManager backupManager;

    private BackupEntryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        ButterKnife.bind(this);
        ((DanteApplication) getApplication()).getAppComponent().inject(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Slide());
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        setupViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.activity_backup_btn_backup)
    protected void onClickBackup() {

        backupManager.backup(bookManager.getAllBooksSync()).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                showSnackbar(getString(R.string.backup_created));
                updateLastBackupTime();
                loadBackupList();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                showSnackbar(getString(R.string.backup_not_created));
            }

            @Override
            public void onNext(Void aVoid) {
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
        ItemTouchHelper.Callback callback = new BackupEntryTouchHelper(adapter);
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
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                                showLongSnackbar(getString(R.string.backup_restore_error));
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                showLongSnackbar(getString(R.string.backup_restored,
                                        ResourceManager.formatTimestamp(entry.getTimestamp())));
                            }
                        });
            }
        });
        rsdf.show(getSupportFragmentManager(), rsdf.getTag());
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

        backupManager.removeBackupEntry(entry).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                showSnackbar(throwable.getMessage());
            }
        }, new Action0() {
            @Override
            public void call() {
                adapter.deleteEntity(position);
                showSnackbar(getString(R.string.backup_removed));
            }
        });
    }

    private void loadBackupList() {
        backupManager.getBackupList().subscribe(new Action1<List<BackupEntry>>() {
            @Override
            public void call(List<BackupEntry> backupEntries) {
                adapter.setData(backupEntries);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
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

    private void showSnackbar(String text) {
        Snackbar.make(rootLayout, text, Snackbar.LENGTH_SHORT).show();
    }

    private void showLongSnackbar(String text) {
        Snackbar.make(rootLayout, text, Snackbar.LENGTH_LONG).show();
    }

}
