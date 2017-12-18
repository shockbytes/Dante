package at.shockbytes.dante.ui.fragment.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.android.gms.common.GoogleApiAvailability;

import at.shockbytes.dante.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * @author Martin Macheiner
 *         Date: 30.08.2016.
 */
public class LegalNoticeDialogFragment extends android.app.DialogFragment {

    private WebView textInfo;
    private ProgressBar progressBar;

    public static LegalNoticeDialogFragment newInstance() {
        return new LegalNoticeDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialogfragment_legal_notice_title);
        builder.setIcon(R.mipmap.ic_legal_notice);
        builder.setView(buildView());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        loadLegalText();
        return builder.create();
    }

    private View buildView() {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialogfragment_legal_notice, null, false);
        textInfo = v.findViewById(R.id.dialogfragment_legal_notice_txt_info);
        progressBar = v.findViewById(R.id.dialogfragment_legal_notice_pb);
        return v;
    }

    private void loadLegalText() {

        Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {

                String licenseInfo = GoogleApiAvailability.getInstance()
                        .getOpenSourceSoftwareLicenseInfo(getActivity());
                licenseInfo = TextUtils.htmlEncode(licenseInfo);
                return Observable.just(licenseInfo);
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        progressBar.setVisibility(View.INVISIBLE);
                        textInfo.loadData(s, "text/html; charset=utf-8", "UTF-8");
                    }
                });
    }

}
