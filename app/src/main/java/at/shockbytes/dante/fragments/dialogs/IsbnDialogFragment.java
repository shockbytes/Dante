package at.shockbytes.dante.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import at.shockbytes.dante.R;

/**
 * @author Martin Macheiner
 *         Date: 30.08.2016.
 */
public class IsbnDialogFragment extends DialogFragment {

    public interface OnIsbnEnteredListener {

        void onIsbnEntered(String isbn);
    }

    private EditText editIsbn;

    private OnIsbnEnteredListener isbnListener;

    public static IsbnDialogFragment newInstance() {
        return new IsbnDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialogfragment_isbn_title);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(R.string.dialogfragment_isbn_message);
        builder.setView(buildView());
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        builder.setPositiveButton(android.R.string.search_go, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isbnListener != null) {
                    isbnListener.onIsbnEntered(editIsbn.getText().toString());
                }
            }
        });

        return builder.create();
    }


    public void setOnIsbnEnteredListener(OnIsbnEnteredListener listener) {
        this.isbnListener = listener;
    }

    private View buildView() {

        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialogfragment_enter_isbn, null, false);
        editIsbn = (EditText) v.findViewById(R.id.dialogfragment_isbn_edit);
        return v;
    }
}
