package at.shockbytes.dante.ui.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.adapter.BookAdapter;
import at.shockbytes.dante.core.DanteApplication;
import at.shockbytes.dante.ui.activity.DetailActivity;
import at.shockbytes.dante.util.books.Book;
import at.shockbytes.dante.util.books.BookListener;
import at.shockbytes.dante.util.books.BookManager;
import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainBookFragment extends Fragment
        implements BookAdapter.OnItemClickListener, BookListener {

    private static final String ARG_STATE = "arg_state";

    @Bind(R.id.fragment_book_main_rv)
    protected RecyclerView recyclerView;

    @Bind(R.id.fragment_book_main_empty_view)
    protected TextView emptyView;

    @Inject
    protected BookManager bookManager;

    private Book.State bookState;
    private BookAdapter bookAdapter;

    private BookAdapter.OnBookPopupItemSelectedListener popupItemSelectedListener;

    public static MainBookFragment newInstance(Book.State state) {
        MainBookFragment fragment =  new MainBookFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(ARG_STATE, state);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            popupItemSelectedListener = (BookAdapter.OnBookPopupItemSelectedListener) context;
        }catch (IllegalStateException exception) {
            Log.e("Dante", "Calling activity must implement OnItemPopupItemSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DanteApplication)getActivity().getApplication()).getAppComponent().inject(this);

        bookState = (Book.State) getArguments().getSerializable(ARG_STATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book_main, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load books async
        bookManager.getBookByState(bookState).subscribe(new Subscriber<List<Book>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e("Dante", "Cannot load books --> " + e.toString());
            }

            @Override
            public void onNext(List<Book> books) {
                if (bookAdapter != null && books != null) {
                    bookAdapter.setData(books);
                    animateEmptyView(false);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void initializeViews() {

        // Initialize text for empty indicator
        String empty = getResources().getStringArray(R.array.empty_indicators)[bookState.ordinal()];
        emptyView.setText(empty);

        // Initialize RecyclerView
        bookAdapter = new BookAdapter(getContext(), new ArrayList<Book>(),
                bookState, popupItemSelectedListener, true);
        recyclerView.setLayoutManager(getLayoutManager());
        bookAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(bookAdapter);
    }

    private RecyclerView.LayoutManager getLayoutManager() {

        if (getResources().getBoolean(R.bool.isTablet)) {
            return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    ? new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    : new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    ? new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
                    : new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }

    }

    @Override
    public void onItemClick(Book t, View v) {
        startActivity(DetailActivity.newIntent(getContext(), t.getId()), getTransitionBundle(v));
    }

    private Bundle getTransitionBundle(View v) {
        return ActivityOptionsCompat
                .makeSceneTransitionAnimation(getActivity(),
                        new Pair<>(v.findViewById(R.id.listitem_book_card),
                                getString(R.string.transition_name_card)),
                        new Pair<>(v.findViewById(R.id.listitem_book_img_thumb),
                                getString(R.string.transition_name_thumb)),
                        new Pair<>(v.findViewById(R.id.listitem_book_txt_title),
                                getString(R.string.transition_name_title)),
                        new Pair<>(v.findViewById(R.id.listitem_book_txt_subtitle),
                                getString(R.string.transition_name_subtitle)),
                        new Pair<>(v.findViewById(R.id.listitem_book_txt_author),
                                getString(R.string.transition_name_author))
            ).toBundle();
    }

    @Override
    public void onBookAdded(Book book) {
        if (bookAdapter != null && book.getState() == bookState) {
            bookAdapter.addEntityAtFirst(book);
            recyclerView.scrollToPosition(0);
            emptyView.setAlpha((bookAdapter.getItemCount() > 0) ? 0 : 1);
        }
    }

    @Override
    public void onBookDeleted(Book book) {
        if (bookAdapter != null) {
            //bookAdapter.deleteEntity(book);
            animateEmptyView(true);
        }
    }

    @Override
    public void onBookStateChanged(Book book, Book.State state) {
        animateEmptyView(true);
    }

    private void animateEmptyView(boolean animate) {

        if (emptyView == null) {
            return;
        }

        if (animate) {
            emptyView.animate()
                    .alpha((bookAdapter.getItemCount() > 0) ? 0 : 1)
                    .setDuration(450)
                    .start();
        } else{
            emptyView.setAlpha((bookAdapter.getItemCount() > 0) ? 0 : 1);
        }
    }

}
