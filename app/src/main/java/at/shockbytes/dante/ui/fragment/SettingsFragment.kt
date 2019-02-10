package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.SwitchPreferenceCompat
import android.widget.Toast
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.util.tracking.Tracker
import at.shockbytes.dante.util.tracking.event.DanteTrackingEvent
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private lateinit var prefsDarkMode: SwitchPreferenceCompat

    @Inject
    lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as? DanteApp)?.appComponent?.inject(this)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.settings)

        prefsDarkMode = findPreference(getString(R.string.prefs_dark_mode_key)) as SwitchPreferenceCompat
        prefsDarkMode.onPreferenceChangeListener = this

        if (BuildConfig.FEATURE_FLAG_CONFIG_VISIBLE) {
            findPreference(getString(R.string.prefs_feature_flag_key)).isVisible = true
        }
    }

    override fun onPreferenceChange(pref: Preference?, newValue: Any?): Boolean {

        if (pref?.key == getString(R.string.prefs_dark_mode_key) && (newValue is Boolean)) {
            tracker.trackEvent(DanteTrackingEvent.DarkModeChangeEvent(!newValue, newValue))
            showDarkModeToast()
        }
        return true
    }

    private fun showDarkModeToast() {
        Toast.makeText(activity, R.string.dark_mode_applied, Toast.LENGTH_LONG).show()
    }

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
