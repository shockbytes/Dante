package at.shockbytes.dante.ui.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import at.shockbytes.dante.R;

public class SettingsFragment extends PreferenceFragment {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
