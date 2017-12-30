package at.shockbytes.dante.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.adapter.BookAdapter;
import at.shockbytes.dante.dagger.AppComponent;
import at.shockbytes.dante.ui.fragment.MainBookFragment;
import at.shockbytes.dante.ui.fragment.dialogs.StatsDialogFragment;
import at.shockbytes.dante.util.AppParams;
import at.shockbytes.dante.util.backup.BackupManager;
import at.shockbytes.dante.util.barcode.QueryCaptureActivity;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookListener;
import at.shockbytes.dante.util.books.BookManager;
import at.shockbytes.dante.util.tracking.Tracker;
import butterknife.BindView;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;

public class MainActivity extends BaseActivity
        implements BookAdapter.OnBookPopupItemSelectedListener,
        TabLayout.OnTabSelectedListener, BackupManager.OnConnectionStatusListener {

    @BindView(R.id.tablayout)
    protected TabLayout tabLayout;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.appbar)
    protected AppBarLayout appBar;

    @BindView(R.id.main_fab)
    protected FloatingActionButton fab;

    @Inject
    protected BookManager bookManager;

    @Inject
    protected BackupManager backupManager;

    @Inject
    protected Tracker tracker;

    @State
    protected int primaryOld;

    @State
    protected int primaryDarkOld;

    @State
    protected int tabPosition;

    private BookListener bookListener;

    private MenuItem menuItemBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        // Fields will be overwritten by icepick if they have already a value
        tabPosition = 1;
        primaryOld = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        primaryDarkOld = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
        Icepick.restoreInstanceState(this, savedInstanceState);

        // Connect to Google Drive for backups
        // TODO Enable later, this will cause an exception for now
        // backupManager.connect(this, this);
    }

    @Override
    public void injectToGraph(@NotNull AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeTabs();
    }

    @Override
    protected void onDestroy() {
        //bookManager.close();

        if (backupManager.isAutoBackupEnabled()) {
            backupManager.backup(bookManager.getAllBooksSync());
        }

        backupManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case AppParams.REQ_CODE_SCAN_BOOK:

                if (resultCode == RESULT_OK) {
                    long bookId = data.getLongExtra(AppParams.EXTRA_BOOK_ID, -1);
                    if (bookId > -1) {
                        bookListener.onBookAdded(bookManager.getBook(bookId));
                    }
                }
                break;

            case BackupManager.RESOLVE_CONNECTION_REQUEST_CODE:

                if (resultCode == RESULT_OK) {
                    backupManager.connect(this, this);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivity(SettingsActivity.newIntent(getApplicationContext()), options.toBundle());
        } else if (id == R.id.action_stats) {
            StatsDialogFragment fragment = StatsDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), "stats-dialog-fragment");
        } else if (id == R.id.action_backup) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivity(BackupActivity.newIntent(this), options.toBundle());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuItemBackup = menu.findItem(R.id.action_backup);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDelete(Book b) {
        bookListener.onBookDeleted(b);
        bookManager.removeBook(b.getId());
    }

    @Override
    public void onShare(Book b) {

        Intent sendIntent = createSharingIntent(b);
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));

        tracker.trackOnBookShared();
    }

    @Override
    public void onMoveToUpcoming(Book b) {
        bookManager.updateBookState(b, Book.State.READ_LATER);
        bookListener.onBookStateChanged(b, Book.State.READ_LATER);
    }

    @Override
    public void onMoveToCurrent(Book b) {
        bookManager.updateBookState(b, Book.State.READING);
        bookListener.onBookStateChanged(b, Book.State.READING);
    }

    @Override
    public void onMoveToDone(Book b) {
        bookManager.updateBookState(b, Book.State.READ);
        bookListener.onBookStateChanged(b, Book.State.READ);

        tracker.trackOnBookMovedToDone(b);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        tabPosition = tab.getPosition();
        MainBookFragment fragment = MainBookFragment.newInstance(Book.State.values()[tabPosition]);
        bookListener = fragment;

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, fragment)
                .commit();

        appBar.setExpanded(true, true);
        toggleFab();
        animateHeader(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @OnClick(R.id.main_fab)
    public void onClickNewBook() {

        tracker.trackOnScanBook();

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(QueryCaptureActivity.newIntent(this),
                AppParams.REQ_CODE_SCAN_BOOK, options.toBundle());
    }

    @Override
    public void onConnectionFailed() {
        menuItemBackup.setVisible(false);
    }

    @Override
    public void onConnected() {
        menuItemBackup.setVisible(true);
    }

    private void initializeTabs() {

        // Select the tab
        tabLayout.addOnTabSelectedListener(this);
        TabLayout.Tab initialTab = tabLayout.getTabAt(tabPosition);
        if (initialTab != null) {
            initialTab.select();
        }

        // Color the controls accordingly
        toolbar.setBackgroundColor(primaryOld);
        appBar.setBackgroundColor(primaryOld);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           getWindow().setStatusBarColor(primaryDarkOld);
        }
    }

    private void toggleFab() {

        fab.hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show();
            }
        }, 300);
    }

    private void animateHeader(int tab) {

        int primary = 0;
        int primaryDark = 0;
        switch (tab) {

            case 0:

                primary = ContextCompat.getColor(this, R.color.tabcolor_upcoming);
                primaryDark = ContextCompat.getColor(this, R.color.tabcolor_upcoming_dark);
                break;

            case 1:

                primary = ContextCompat.getColor(this, R.color.tabcolor_current);
                primaryDark = ContextCompat.getColor(this, R.color.tabcolor_current_dark);
                break;

            case 2:

                primary = ContextCompat.getColor(this, R.color.tabcolor_done);
                primaryDark = ContextCompat.getColor(this, R.color.tabcolor_done_dark);
                break;
        }

        ObjectAnimator animatorAppBar = ObjectAnimator.ofObject(appBar, "backgroundColor",
                new ArgbEvaluator(), primaryOld, primary)
                .setDuration(300);
        ObjectAnimator animatorToolbar = ObjectAnimator.ofObject(toolbar, "backgroundColor",
                new ArgbEvaluator(), primaryOld, primary)
                .setDuration(300);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                primaryDarkOld, primaryDark)
                .setDuration(300);
        // Supress lint, because we are only setting listener, when api is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @SuppressLint("NewApi")
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    getWindow().setStatusBarColor((Integer) valueAnimator.getAnimatedValue());
                }
            });
        }

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorAppBar, animatorToolbar, colorAnimation);
        set.start();

        primaryOld = primary;
        primaryDarkOld = primaryDark;
    }

    private Intent createSharingIntent(Book b) {

        String msg = getString(R.string.share_template, b.getTitle(), b.getGoogleBooksLink());
        return new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, msg)
                .setType("text/plain");
    }

}