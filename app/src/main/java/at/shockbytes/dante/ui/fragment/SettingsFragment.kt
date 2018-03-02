package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import at.shockbytes.dante.R

class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private lateinit var prefsTracking: SwitchPreference
    private lateinit var prefsOverlay: SwitchPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        prefsTracking = findPreference(getString(R.string.prefs_page_tracking_key)) as SwitchPreference
        prefsOverlay = findPreference(getString(R.string.prefs_page_overlay_key)) as SwitchPreference

        prefsTracking.onPreferenceChangeListener = this
        prefsOverlay.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(pref: Preference?, newValue: Any?): Boolean {

        if (pref?.key == getString(R.string.prefs_page_tracking_key) && !(newValue as Boolean)) {
            prefsOverlay.isChecked = false
        }

        return true
    }

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

}
