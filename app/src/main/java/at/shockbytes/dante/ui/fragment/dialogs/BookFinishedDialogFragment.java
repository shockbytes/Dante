package at.shockbytes.dante.ui.fragment.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import at.shockbytes.dante.R;

/**
 * @author Martin Macheiner
 *         Date: 16.09.2017.
 */
public class BookFinishedDialogFragment extends DialogFragment {

    public interface OnBookMoveFinishedListener {

        void onBookMoveAccepted();
    }

    private static final String ARG_TITLE = "title";

    private String bookTitle;

    private OnBookMoveFinishedListener listener;

    public static BookFinishedDialogFragment newInstance(String bookTitle) {
        BookFinishedDialogFragment fragment = new BookFinishedDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_TITLE, bookTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookTitle = getArguments().getString(ARG_TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.book_finished, bookTitle));
        builder.setIcon(R.drawable.ic_pick_done);
        builder.setMessage(R.string.book_finished_move_to_done_question);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (listener != null) {
                    listener.onBookMoveAccepted();
                }
                dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }

    public BookFinishedDialogFragment setOnBookMoveFinishedListener(OnBookMoveFinishedListener listener) {
        this.listener = listener;
        return this;
    }

}
