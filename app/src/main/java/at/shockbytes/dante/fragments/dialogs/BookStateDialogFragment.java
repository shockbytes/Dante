package at.shockbytes.dante.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import at.shockbytes.dante.R;
import at.shockbytes.dante.util.books.Book;

/**
 * @author Martin Macheiner
 *         Date: 28.08.2016.
 */
public class BookStateDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface OnBookStateClickedListener {
        void onBookStateClicked(Book.State state);
    }

    private static final String ARG_TITLE = "arg_title";

    private String title;

    private OnBookStateClickedListener listener;

    public static BookStateDialogFragment newInstance(@NonNull String title) {

        BookStateDialogFragment fragment = new BookStateDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString(ARG_TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setView(buildView());
        builder.setCancelable(true);

        return builder.create();
    }

    @Override
    public void onClick(View view) {

        // Do nothing when no listener is attached
        if (listener == null) {
            dismiss();
            return;
        }

        Book.State state = Book.State.READ_LATER;
        switch (view.getId()) {

            case R.id.dialogfragment_btn_upcoming:

                state = Book.State.READ_LATER;
                break;

            case R.id.dialogfragment_btn_current:

                state = Book.State.READING;
                break;

            case R.id.dialogfragment_btn_done:

                state = Book.State.READ;
                break;

        }
        listener.onBookStateClicked(state);
        dismiss();
    }

    private View buildView() {

        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialogfragment_bookstate,
                null, false);

        Button btnUpcoming = (Button) v.findViewById(R.id.dialogfragment_btn_upcoming);
        Button btnCurrent = (Button) v.findViewById(R.id.dialogfragment_btn_current);
        Button btnDone = (Button) v.findViewById(R.id.dialogfragment_btn_done);

        btnUpcoming.setOnClickListener(this);
        btnCurrent.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        return v;
    }

    public void setOnBookStateClickedListener(OnBookStateClickedListener listener) {
        this.listener = listener;
    }

}
