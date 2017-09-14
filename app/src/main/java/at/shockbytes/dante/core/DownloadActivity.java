package at.shockbytes.dante.core;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.fragments.DownloadBookFragment;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.tracking.Tracker;

public class DownloadActivity extends AppCompatActivity
        implements DownloadBookFragment.OnBookDownloadedListener {

    private static final String ARG_QUERY = "arg_barcode";
    public static final String EXTRA_BOOK_ID = "extra_book_downloaded";

    public static Intent newIntent(Context context, String query) {
        return new Intent(context, DownloadActivity.class).putExtra(ARG_QUERY, query);
    }

    @Inject
    protected Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ((DanteApplication) getApplication()).getAppComponent().inject(this);

        String query = getIntent().getExtras().getString(ARG_QUERY);
        if (query != null) {
            showDownloadFragment(query);
        } else {
            setResult(RESULT_CANCELED);
            supportFinishAfterTransition();
        }
    }

    @Override
    public void onBookDownloaded(Book book) {
        tracker.trackOnBookScanned(book);
        forwardToCaller(book.getId(), true);
    }

    @Override
    public void onCancelDownload() {
        tracker.trackOnFoundBookCanceled();
        forwardToCaller(-1, false);
    }

    @Override
    public void onErrorDownload(String reason) {
        tracker.trackOnDownloadError(reason);
    }

    @Override
    public void onCloseOnError() {
        forwardToCaller(-1, false);
    }

    public void colorSystemBars(int actionBarColor, int actionBarTextColor, int statusBarColor, String bookTitle) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
            Spannable text = new SpannableString(bookTitle);
            text.setSpan(new ForegroundColorSpan(actionBarTextColor), 0, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(text);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(statusBarColor);
        }

    }

    private void forwardToCaller(long bookId, boolean isSuccessful) {

        int resultCode = isSuccessful ? RESULT_OK : RESULT_CANCELED;
        setResult(resultCode, new Intent().putExtra(EXTRA_BOOK_ID, bookId));
        supportFinishAfterTransition();
    }

    private void showDownloadFragment(String query) {

        DownloadBookFragment fragment = DownloadBookFragment.newInstance(query);
        fragment.setOnBookDownloadedListener(this);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.activity_download_main_content, fragment)
                .commit();
    }

}
