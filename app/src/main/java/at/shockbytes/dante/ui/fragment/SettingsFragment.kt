package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.UrlLauncher
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.tracking.Tracker
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    @Inject
    lateinit var danteSettings: DanteSettings

    @Inject
    lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as? DanteApp)?.appComponent?.inject(this)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.settings)

        findPreference<ListPreference>(getString(R.string.prefs_dark_mode_key))?.apply {
            this.onPreferenceChangeListener = this@SettingsFragment
            summary = this.entry
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.prefs_tracking_key))?.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean) {
                    tracker.isTrackingAllowed = newValue
                }
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_change_icon_key))?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {

                val fragment = LauncherIconPickerFragment
                    .newInstance()
                    .setOnDismissListener(::updateLauncherIconSummary)

                DanteUtils.addFragmentToActivity(
                    parentFragmentManager,
                    fragment,
                    android.R.id.content,
                    addToBackStack = true
                )
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_contribute_code_key))?.apply {
            this.setOnPreferenceClickListener {
                UrlLauncher.openDanteGithubPage(requireContext())
                true
            }
        }
        findPreference<Preference>(getString(R.string.prefs_translation_key))?.apply {
            isVisible = false

            /* Discontinued until a feasible solution is found
            this.setOnPreferenceClickListener {
                MailLauncher.sendMail(requireActivity(), getString(R.string.mail_subject_translation), getString(R.string.mail_body_translation))
                true
            }
             */
        }

        showFeatureFlagsConfig(BuildConfig.DEBUG)
    }

    private fun showFeatureFlagsConfig(show: Boolean) {

        val featureFlagPreference = findPreference<Preference>(getString(R.string.prefs_feature_flag_key))
        featureFlagPreference?.isVisible = show

        if (show) {
            featureFlagPreference?.isVisible = true
            featureFlagPreference?.setOnPreferenceClickListener {
                DanteUtils.addFragmentToActivity(
                    parentFragmentManager,
                    FeatureFlagConfigFragment.newInstance(),
                    android.R.id.content,
                    addToBackStack = true
                )
                true
            }
        }
    }

    override fun onPreferenceChange(pref: Preference?, newValue: Any?): Boolean {

        if (pref?.key == getString(R.string.prefs_dark_mode_key)) {
            showToast(getString(R.string.dark_mode_applied))
        }
        return true
    }

    private fun showToast(content: String) {
        Toast.makeText(activity, content, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        updateLauncherIconSummary()
    }

    private fun updateLauncherIconSummary() {
        findPreference<Preference>(getString(R.string.prefs_change_icon_key))?.apply {
            summary = danteSettings.selectedLauncherIconState.title
        }
    }

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
