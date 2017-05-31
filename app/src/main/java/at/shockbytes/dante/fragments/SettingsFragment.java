package at.shockbytes.dante.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import at.shockbytes.dante.R;
import at.shockbytes.dante.fragments.dialogs.LegalNoticeDialogFragment;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
        findPreference(getString(R.string.prefs_open_source_licenses_key))
                .setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        LegalNoticeDialogFragment lndf = LegalNoticeDialogFragment.newInstance();
        lndf.show(getFragmentManager(), lndf.getTag());
        return true;
    }
}
