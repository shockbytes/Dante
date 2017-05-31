package at.shockbytes.dante.fragments.dialogs;

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
 *         Date: 30.08.2016.
 */
public class DownloadErrorDialogFragment extends DialogFragment {

    private static final String ARG_ERROR = "arg_error";

    private String error;

    public static DownloadErrorDialogFragment newInstance(String error) {
        DownloadErrorDialogFragment fragment = new DownloadErrorDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_ERROR, error);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        error = getArguments().getString(ARG_ERROR);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.download_error);
        builder.setIcon(R.drawable.ic_download_error);
        builder.setMessage(error);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }

}
