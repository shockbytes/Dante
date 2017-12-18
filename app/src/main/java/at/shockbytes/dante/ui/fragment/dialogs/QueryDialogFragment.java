package at.shockbytes.dante.ui.fragment.dialogs;

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
public class QueryDialogFragment extends DialogFragment {

    public interface OnQueryEnteredListener {

        void onQueryEntered(String isbn);
    }

    private EditText editQuery;

    private OnQueryEnteredListener queryListener;

    public static QueryDialogFragment newInstance() {
        return new QueryDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialogfragment_query_title);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(R.string.dialogfragment_query_message);
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
                if (queryListener != null) {
                    // Remove blanks with + so query works also for titles
                    queryListener.onQueryEntered(editQuery.getText().toString().replace(' ', '+'));
                }
            }
        });

        return builder.create();
    }


    public void setOnQueryEnteredListener(OnQueryEnteredListener listener) {
        this.queryListener = listener;
    }

    private View buildView() {

        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialogfragment_enter_query, null, false);
        editQuery = v.findViewById(R.id.dialogfragment_isbn_edit);
        return v;
    }
}
