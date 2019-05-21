package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import android.widget.Toast
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.tracking.Tracker
import at.shockbytes.dante.tracking.event.DanteTrackingEvent
import javax.inject.Inject
import android.content.Intent
import android.net.Uri

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    @Inject
    lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as? DanteApp)?.appComponent?.inject(this)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.settings)

        (findPreference(getString(R.string.prefs_dark_mode_key)) as SwitchPreferenceCompat).apply {
            this.onPreferenceChangeListener = this@SettingsFragment
        }

        findPreference(getString(R.string.prefs_contribute_key)).apply {
            this.setOnPreferenceClickListener {
                openDanteGithubPage()
                true
            }
        }

        showFeatureFlagsConfig(BuildConfig.DEBUG)
    }

    private fun openDanteGithubPage() {
        val url = getString(at.shockbytes.dante.R.string.dante_github_link)
        val githubIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
        startActivity(githubIntent)
    }

    private fun showFeatureFlagsConfig(show: Boolean) {

        val featureFlagPreference = findPreference(getString(R.string.prefs_feature_flag_key))
        featureFlagPreference.isVisible = show

        if (show) {
            featureFlagPreference.isVisible = true
            featureFlagPreference.setOnPreferenceClickListener {
                fragmentManager?.let { fm ->
                    DanteUtils.addFragmentToActivity(
                            fm,
                            FeatureFlagConfigFragment.newInstance(),
                            android.R.id.content,
                            addToBackStack = true
                    )
                }
                true
            }
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
