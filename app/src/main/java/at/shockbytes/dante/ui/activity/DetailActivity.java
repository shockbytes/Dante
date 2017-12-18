package at.shockbytes.dante.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.core.DanteApplication;
import at.shockbytes.dante.ui.fragment.dialogs.BookFinishedDialogFragment;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookManager;
import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements Callback, Palette.PaletteAsyncListener, SeekBar.OnSeekBarChangeListener {

    private static final String ARG_ID = "arg_id";

    private Book book;

    @Inject
    protected BookManager manager;

    @Inject
    protected SharedPreferences prefs;

    @Bind(R.id.activity_detail_img_thumb)
    protected ImageView imgViewThumb;

    @Bind(R.id.activity_detail_txt_title)
    protected TextView txtTitle;

    @Bind(R.id.activity_detail_txt_subtitle)
    protected TextView txtSubTitle;

    @Bind(R.id.activity_detail_txt_author)
    protected TextView txtAuthor;

    @Bind(R.id.activity_detail_txt_pages)
    protected TextView txtPages;

    @Bind(R.id.activity_detail_txt_published)
    protected TextView txtPublished;

    @Bind(R.id.activity_detail_txt_isbn)
    protected TextView txtIsbn;

    @Bind(R.id.activity_detail_cardview_dates)
    protected CardView cardViewDates;

    @Bind(R.id.activity_detail_txt_wishlist_date)
    protected TextView txtWishlistdate;

    @Bind(R.id.activity_detail_txt_start_date)
    protected TextView txtStartdate;

    @Bind(R.id.activity_detail_txt_end_date)
    protected TextView txtEnddate;

    @Bind(R.id.activity_detail_seekbar_pages)
    protected AppCompatSeekBar seekBarPages;

    public static Intent newIntent(Context context, long id) {
        return new Intent(context, DetailActivity.class).putExtra(ARG_ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ((DanteApplication)getApplication()).getAppComponent().inject(this);
        ButterKnife.bind(this);

        long id = getIntent().getLongExtra(ARG_ID, -1);
        if (id == -1) {
            Toast.makeText(getApplicationContext(), R.string.error_load_book, Toast.LENGTH_LONG).show();
            supportFinishAfterTransition();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        book = manager.getBook(id);
        initialize();
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

    private void initialize() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(book.getTitle());
        }

        initializeMainCard();
        initializeTimeCard();
    }

    private void initializeMainCard() {

        txtTitle.setText(book.getTitle());
        txtAuthor.setText(book.getAuthor());

        txtPublished.setText(!book.getPublishedDate().isEmpty() ? book.getPublishedDate() : "---");
        txtIsbn.setText(!book.getIsbn().isEmpty() ? book.getIsbn() : "---");

        // Hide subtitle if not available
        String subtitle = book.getSubTitle();
        if (subtitle.isEmpty()) {
            txtSubTitle.setVisibility(View.GONE);
        } else {
            txtSubTitle.setText(subtitle);
        }

        String thumbnailAddress = book.getThumbnailAddress();
        if (thumbnailAddress != null && !thumbnailAddress.isEmpty()) {
            Picasso.with(getApplicationContext()).load(thumbnailAddress)
                    .placeholder(R.drawable.ic_placeholder).into(imgViewThumb, this);
        }

        // Setup pages and SeekBar
        String pages = book.getState() == Book.State.READING
                ? getString(R.string.detail_pages, book.getCurrentPage(), book.getPageCount())
                : String.valueOf(book.getPageCount());
        txtPages.setText(pages);

        // Book must be in reading state and must have a legit page count and overall the feature
        // must be enabled in the settings
        if (prefs.getBoolean(getString(R.string.prefs_page_tracking_key), true)
                && book.getState() == Book.State.READING
                && book.getPageCount() > 0) {
            seekBarPages.setMax(book.getPageCount());
            seekBarPages.setProgress(book.getCurrentPage());
            seekBarPages.setOnSeekBarChangeListener(this);
        } else {
            seekBarPages.setVisibility(View.GONE);
        }
    }

    private void initializeTimeCard() {

        // Hide complete card if no time information is available
        if (!book.isAnyTimeInformationAvailable()) {
            cardViewDates.setVisibility(View.GONE);
        } else {

            String pattern = "dd. MMM yyyy";
            // Check if wishlist date is available
            if (book.getWishlistDate() > 0) {
                String wishlistDate = new DateTime(book.getWishlistDate()).toString(pattern);
                txtWishlistdate.setText(getString(R.string.detail_wishlist_date, wishlistDate));
            } else {
                txtWishlistdate.setVisibility(View.GONE);
            }

            // Check if start date is available
            if (book.getStartDate() > 0) {
                String startDate = new DateTime(book.getStartDate()).toString(pattern);
                txtStartdate.setText(getString(R.string.detail_start_date, startDate));
            } else {
                txtStartdate.setVisibility(View.GONE);
            }

            // Check if end date is available
            if (book.getEndDate() > 0) {
                String endDate = new DateTime(book.getEndDate()).toString(pattern);
                txtEnddate.setText(getString(R.string.detail_end_date, endDate));
            } else {
                txtEnddate.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onSuccess() {

        if (imgViewThumb != null && imgViewThumb.getDrawable() != null) {
            Bitmap bm = ((BitmapDrawable) imgViewThumb.getDrawable()).getBitmap();
            if (bm != null) {
                Palette.from(bm).generate(this);
            }
        }
    }

    @Override
    public void onError() {

    }

    @Override
    public void onGenerated(Palette palette) {

        // Check for NullPointer
        if (palette.getLightMutedSwatch() == null || palette.getDarkMutedSwatch() == null) {
            return;
        }

        int actionBarColor = palette.getLightMutedSwatch().getRgb();
        int actionBarTextColor = palette.getLightMutedSwatch().getTitleTextColor();
        int statusBarColor = palette.getDarkMutedSwatch().getRgb();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
            Spannable text = new SpannableString(book.getTitle());
            text.setSpan(new ForegroundColorSpan(actionBarTextColor), 0, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(text);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(statusBarColor);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        txtPages.setText(getString(R.string.detail_pages, i, book.getPageCount()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        manager.updateCurrentBookPage(book, seekBar.getProgress());
        if (book.getCurrentPage() == book.getPageCount()) {
            BookFinishedDialogFragment.newInstance(book.getTitle())
                    .setOnBookMoveFinishedListener(new BookFinishedDialogFragment.OnBookMoveFinishedListener() {
                        @Override
                        public void onBookMoveAccepted() {
                            manager.updateBookState(book, Book.State.READ);
                            supportFinishAfterTransition();
                        }
                    })
                    .show(getSupportFragmentManager(), "book_finished_fragment");
        }
    }
}
