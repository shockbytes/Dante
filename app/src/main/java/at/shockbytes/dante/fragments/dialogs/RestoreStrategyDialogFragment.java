package at.shockbytes.dante.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import at.shockbytes.dante.R;
import at.shockbytes.dante.util.backup.BackupManager;

/**
 * @author Martin Macheiner
 *         Date: 30.08.2016.
 */
public class RestoreStrategyDialogFragment extends DialogFragment
        implements RadioGroup.OnCheckedChangeListener {


    public interface OnRestoreStrategySelectedListener {

        void onRestoreStrategySelected(BackupManager.RestoreStrategy strategy);
    }

    private TextView textInfo;
    private RadioGroup radioGroup;

    private OnRestoreStrategySelectedListener strategyListener;

    public static RestoreStrategyDialogFragment newInstance() {
        return new RestoreStrategyDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialogfragment_restore_strategy_title);
        builder.setIcon(R.mipmap.ic_google_drive);
        builder.setView(buildView());
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (strategyListener != null) {
                    strategyListener.onRestoreStrategySelected(getSelectedStrategy());
                }
            }
        });

        return builder.create();
    }


    public void setOnRestoreStrategySelectedListener(OnRestoreStrategySelectedListener listener) {
        this.strategyListener = listener;
    }

    private View buildView() {

        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialogfragment_restore_strategy, null, false);
        radioGroup = (RadioGroup) v.findViewById(R.id.dialogfragment_restore_strategy_radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        textInfo = (TextView) v.findViewById(R.id.dialogfragment_restore_strategy_txt_info);
        return v;
    }

    private BackupManager.RestoreStrategy getSelectedStrategy() {

        BackupManager.RestoreStrategy strategy = null;
        switch (radioGroup.getCheckedRadioButtonId()) {

            case R.id.dialogfragment_restore_strategy_radio_merge:
                strategy = BackupManager.RestoreStrategy.MERGE;
                break;

            case R.id.dialogfragment_restore_strategy_radio_overwrite:
                strategy = BackupManager.RestoreStrategy.OVERWRITE;
                break;
        }
        return strategy;
    }

    private String getInfoStringForStrategy() {

        String info = null;
        switch (radioGroup.getCheckedRadioButtonId()) {

            case R.id.dialogfragment_restore_strategy_radio_merge:
                info = getString(R.string.backup_info_merge);
                break;

            case R.id.dialogfragment_restore_strategy_radio_overwrite:
                info = getString(R.string.backup_info_overwrite);
                break;
        }
        return info;
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        textInfo.setText(getInfoStringForStrategy());
    }
}
