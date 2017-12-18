package at.shockbytes.dante.ui.fragment;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.adapter.BookAdapter;
import at.shockbytes.dante.core.DanteApplication;
import at.shockbytes.dante.ui.activity.DownloadActivity;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookManager;
import at.shockbytes.dante.util.books.BookSuggestion;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;


public class DownloadBookFragment extends Fragment
        implements Callback, Palette.PaletteAsyncListener, BookAdapter.OnItemClickListener {

    public interface OnBookDownloadedListener {

        void onBookDownloaded(Book book);

        void onCancelDownload();

        void onErrorDownload(String reason);

        void onCloseOnError();

    }

    private static final String ARG_BARCODE = "arg_barcode";

    public static DownloadBookFragment newInstance(String barcode) {
        DownloadBookFragment fragment = new DownloadBookFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BARCODE, barcode);
        fragment.setArguments(args);
        return fragment;
    }

    @Bind({ R.id.fragment_download_book_btn_upcoming,
            R.id.fragment_download_book_btn_current,
            R.id.fragment_download_book_btn_done,
            R.id.fragment_download_book_btn_not_my_book})
    List<View> animViews;

    @Bind(R.id.fragment_download_book_imgview_cover)
    protected ImageView imgViewCover;

    @Bind(R.id.fragment_download_book_txt_title)
    protected TextView txtTitle;

    @Bind(R.id.fragment_download_book_rv_other_suggestions)
    protected RecyclerView rvOtherSuggestions;

    @Bind(R.id.fragment_download_book_txt_other_suggestions)
    protected TextView txtOtherSuggestions;

    @Bind(R.id.fragment_download_book_btn_not_my_book)
    protected Button btnNotMyBook;

    @Bind(R.id.fragment_download_book_progressbar)
    protected ProgressBar progressBar;

    @Bind(R.id.fragment_download_book_main_view)
    protected View mainView;

    @Bind(R.id.fragment_download_book_error_view)
    protected View errorView;

    @Bind(R.id.fragment_download_book_txt_error_cause)
    protected TextView txtErrorCause;

    @Inject
    protected BookManager bookManager;

    private BookAdapter bookAdapter;

    private String query;

    private OnBookDownloadedListener listener;

    private Book selectedBook;

    private boolean isOtherSuggestionsShowing;

    public DownloadBookFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DanteApplication) getActivity().getApplication()).getAppComponent().inject(this);
        if (getArguments() != null) {
            query = getArguments().getString(ARG_BARCODE);
        }
        isOtherSuggestionsShowing = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_download_book, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        downloadBook();
    }

    @Override
    public void onSuccess() {
        Bitmap bm = ((BitmapDrawable) imgViewCover.getDrawable()).getBitmap();
        if (bm != null) {
            Palette.from(bm).generate(this);
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

        ((DownloadActivity) getActivity()).colorSystemBars(actionBarColor, actionBarTextColor,
                statusBarColor, selectedBook.getTitle());
    }

    @Override
    public void onItemClick(Book t, View v) {
        int index = bookAdapter.getLocation(t);
        bookAdapter.deleteEntity(t);
        bookAdapter.addEntity(index, selectedBook);

        selectedBook = t;
        setTitleAndIcon(selectedBook);
        rvOtherSuggestions.scrollToPosition(0);
    }


    @OnClick(R.id.fragment_download_book_btn_error_close)
    protected void onClickCloseError() {
        listener.onCloseOnError();
    }

    @OnClick(R.id.fragment_download_book_btn_not_my_book)
    protected void onClickNotMyBook() {

        if (!isOtherSuggestionsShowing) {

            txtTitle.animate().translationY(0).setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(new Runnable() {
                @Override
                public void run() {
                    rvOtherSuggestions.animate().alpha(1).start();
                    txtOtherSuggestions.animate().alpha(1).start();
                }
            }).start();
            imgViewCover.animate().translationY(0).setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            btnNotMyBook.setText(R.string.download_suggestions_none);

            isOtherSuggestionsShowing = true;
        } else {
            listener.onCancelDownload();
        }

    }

    @OnClick(R.id.fragment_download_book_btn_upcoming)
    protected void onClickUpcoming() {
        finishBookDownload(Book.State.READ_LATER);
    }

    @OnClick(R.id.fragment_download_book_btn_current)
    protected void onClickCurrent() {
        finishBookDownload(Book.State.READING);
    }

    @OnClick(R.id.fragment_download_book_btn_done)
    protected void onClickDone() {
        finishBookDownload(Book.State.READ);
    }

    public void setOnBookDownloadedListener(OnBookDownloadedListener listener) {
        this.listener = listener;
    }

    private void finishBookDownload(Book.State bookState) {

        // Set the state and store it in database
        selectedBook.setState(bookState);
        selectedBook = bookManager.addBook(selectedBook); // Return the object with set ID

        listener.onBookDownloaded(selectedBook);
    }

    private void downloadBook() {

        bookManager.downloadBook(query).subscribe(new Action1<BookSuggestion>() {
            @Override
            public void call(final BookSuggestion suggestion) {

                animateBookViews();
                if (suggestion != null && suggestion.hasSuggestions()) {
                    selectedBook = suggestion.getMainSuggestion();
                    setTitleAndIcon(selectedBook);
                    setupOtherSuggestionsRecyclerView(suggestion.getOtherSuggestions());
                } else {
                    listener.onErrorDownload("no suggestions");
                    showErrorLayout(getString(R.string.download_book_json_error));
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                showErrorLayout(throwable);
                listener.onErrorDownload(throwable.getMessage());
            }
        });
    }

    private void animateBookViews() {

        // Hide progressbar smoothly
        progressBar.animate().alpha(0).scaleY(0.5f).scaleX(0.5f).setDuration(500).start();

        ButterKnife.apply(animViews, new ButterKnife.Action<View>() {
            @Override
            public void apply(View view, int index) {
                view.animate().scaleY(1f).scaleX(1f).alpha(1)
                        .setInterpolator(new OvershootInterpolator(4f))
                        .setStartDelay(index * 150).setDuration(250).start();
            }
        });

    }

    private void setTitleAndIcon(Book mainBook) {

        txtTitle.setText(mainBook.getTitle());

        if (mainBook.getThumbnailAddress() != null && !mainBook.getThumbnailAddress().isEmpty()) {
            Picasso.with(getContext()).load(mainBook.getThumbnailAddress())
                    .placeholder(R.drawable.ic_placeholder).into(imgViewCover, this);
        } else {
            imgViewCover.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private void setupOtherSuggestionsRecyclerView(List<Book> books) {

        bookAdapter = new BookAdapter(getContext(), books,
                Book.State.READ, null, false);
        rvOtherSuggestions.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        bookAdapter.setOnItemClickListener(this);
        rvOtherSuggestions.setAdapter(bookAdapter);
    }

    private void showErrorLayout(Throwable error) {

        String cause = getString(R.string.download_code_error);
        showErrorLayout(cause);
    }

    private void showErrorLayout(String cause) {
        mainView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        errorView.animate().alpha(1).start();
        txtErrorCause.setText(cause);
    }


}
