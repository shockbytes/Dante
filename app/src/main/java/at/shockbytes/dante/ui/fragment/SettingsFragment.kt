package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import at.shockbytes.dante.BuildConfig
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.DanteUtils.dpToPixelF
import at.shockbytes.dante.util.MailLauncher
import at.shockbytes.dante.util.UrlLauncher
import at.shockbytes.dante.util.getStringList
import at.shockbytes.dante.util.settings.DanteSettings
import at.shockbytes.dante.util.sort.SortStrategy
import at.shockbytes.tracking.Tracker
import at.shockbytes.tracking.event.DanteTrackingEvent
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    @Inject
    lateinit var danteSettings: DanteSettings

    @Inject
    lateinit var tracker: Tracker

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity?.application as? DanteApp)?.appComponent?.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.setBackgroundResource(R.color.colorActionBar)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.settings)

        findPreference<ListPreference>(getString(R.string.prefs_dark_mode_key))?.apply {
            this.onPreferenceChangeListener = this@SettingsFragment
            summary = this.entry
        }

        findPreference<Preference>(getString(R.string.prefs_sort_strategy_key))?.apply {
            this.summary = getString(R.string.sorted_by, getString(danteSettings.sortStrategy.displayTitle))

            onPreferenceClickListener = Preference.OnPreferenceClickListener {

                MaterialDialog(requireContext())
                    .title(R.string.dialogfragment_sort_by)
                    .listItemsSingleChoice(
                        items = getStringList(R.array.sort_strategy),
                        initialSelection = danteSettings.sortStrategy.ordinal
                    ) { _, index, _ ->

                        val sortStrategy = SortStrategy.values()[index]
                        danteSettings.sortStrategy = sortStrategy
                        this.summary = getString(R.string.sorted_by, getString(sortStrategy.displayTitle))
                    }
                    .icon(R.drawable.ic_sort)
                    .cornerRadius(context.dpToPixelF(6))
                    .cancelOnTouchOutside(true)
                    .positiveButton(R.string.apply) {
                        it.dismiss()
                    }
                    .show()

                true
            }
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.prefs_tracking_key))?.apply {
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean) {
                    tracker.isTrackingAllowed = newValue
                }
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_privacy_policy_key))?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                UrlLauncher.openPrivacyPolicy(requireContext())
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_terms_of_services_key))?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                UrlLauncher.openTermsOfServicePage(requireContext())
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_contribute_code_key))?.apply {
            this.setOnPreferenceClickListener {
                UrlLauncher.openDanteGithubPage(requireContext())
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_community_key))?.apply {
            this.setOnPreferenceClickListener {
                UrlLauncher.openDiscordPage(requireContext())
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_ad_free_medium_article_key))?.apply {
            this.setOnPreferenceClickListener {
                tracker.track(DanteTrackingEvent.OpenAdFreeMediumArticle)
                UrlLauncher.openAdFreeMediumArticle(requireContext())
                true
            }
        }

        findPreference<Preference>(getString(R.string.prefs_feedback_key))?.apply {
            this.setOnPreferenceClickListener {
                MailLauncher.sendMail(
                    requireActivity(),
                    subject = getString(R.string.mail_feedback),
                    attachVersion = true
                )
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

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
