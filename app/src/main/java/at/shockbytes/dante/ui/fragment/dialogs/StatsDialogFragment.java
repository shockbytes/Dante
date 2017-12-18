package at.shockbytes.dante.ui.fragment.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import javax.inject.Inject;

import at.shockbytes.dante.R;
import at.shockbytes.dante.core.DanteApplication;
import at.shockbytes.dante.util.AppParams;
import at.shockbytes.dante.util.books.BookManager;

/**
 * @author Martin Macheiner
 *         Date: 29.08.2016.
 */
public class StatsDialogFragment extends DialogFragment {

    @Inject
    protected BookManager bookManager;

    public static StatsDialogFragment newInstance() {
        return new StatsDialogFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DanteApplication)getActivity().getApplication()).getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.label_stats);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setView(buildView());
        builder.setCancelable(true);
        return builder.create();
    }

    private View buildView() {

        Map<String, Integer> stats = bookManager.getStatistics();
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_stats, null, false);

        TextView txtUpcoming = v.findViewById(R.id.dialogfragment_stats_txt_upcoming);
        TextView txtCurrent = v.findViewById(R.id.dialogfragment_stats_txt_current);
        TextView txtDone = v.findViewById(R.id.dialogfragment_stats_txt_done);
        TextView txtPages = v.findViewById(R.id.dialogfragment_stats_txt_pages);

        String upcoming = getString(R.string.dialogfragment_stats_upcoming,
                stats.get(AppParams.STAT_UPCOMING));
        String current = getString(R.string.dialogfragment_stats_current,
                stats.get(AppParams.STAT_CURRENT));
        String done = getString(R.string.dialogfragment_stats_done,
                stats.get(AppParams.STAT_DONE));
        String pages = getString(R.string.dialogfragment_stats_pages,
                stats.get(AppParams.STAT_PAGES));

        txtUpcoming.setText(upcoming);
        txtCurrent.setText(current);
        txtDone.setText(done);
        txtPages.setText(pages);

        return v;
    }

}
