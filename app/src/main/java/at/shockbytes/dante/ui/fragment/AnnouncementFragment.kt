package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.announcement.Announcement
import at.shockbytes.dante.databinding.FragmentAnnouncementBinding
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.ui.viewmodel.AnnouncementViewModel
import at.shockbytes.dante.util.MailLauncher
import at.shockbytes.dante.util.UrlLauncher
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOfActivity
import com.airbnb.lottie.LottieDrawable
import javax.inject.Inject

class AnnouncementFragment : BaseFragment<FragmentAnnouncementBinding>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentAnnouncementBinding {
        return FragmentAnnouncementBinding.inflate(inflater, root, attachToRoot)
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AnnouncementViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOfActivity(requireActivity(), vmFactory)
    }

    override fun setupViews() = Unit

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestAnnouncementState()

        viewModel.getAnnouncementState().observe(this, { announcementState ->
            when (announcementState) {
                is AnnouncementViewModel.AnnouncementState.Active -> {
                    populateAnnouncementViews(announcementState.announcement)
                }
                is AnnouncementViewModel.AnnouncementState.Inactive -> {
                    closeModal()
                }
            }
        })
    }

    private fun populateAnnouncementViews(announcement: Announcement) {
        with(announcement) {
            vb.tvAnnouncementTitle.setText(titleRes)
            vb.tvAnnouncementDescription.setText(descriptionRes)

            when (illustration) {
                is Announcement.Illustration.LottieIllustration -> {
                    vb.ivAnnouncement.setVisible(false)
                    vb.lottieAnnouncement.apply {
                        setVisible(true)
                        repeatCount = LottieDrawable.INFINITE
                        setAnimation(illustration.lottieRes)
                    }
                }
                is Announcement.Illustration.ImageIllustration -> {
                    vb.lottieAnnouncement.setVisible(false, invisibilityState = View.INVISIBLE)
                    vb.ivAnnouncement.apply {
                        setVisible(true)
                        setImageResource(illustration.drawableRes)
                    }
                }
            }

            vb.btnAnnouncementAction.apply {
                setVisible(hasAction)

                this@with.action?.let { action ->
                    setOnClickListener {
                        performAnnouncementAction(action)
                    }
                    setText(action.actionLabel)
                }
            }

            vb.layoutAnnouncement.setOnClickListener {
                closeModalAndDismissAnnouncement(this)
            }
            vb.btnAnnouncementClose.setOnClickListener {
                closeModalAndDismissAnnouncement(this)
            }
        }
    }

    private fun performAnnouncementAction(action: Announcement.Action) {
        when (action) {
            is Announcement.Action.OpenUrl -> {
                UrlLauncher.launchUrl(requireContext(), action.url)
            }
            is Announcement.Action.Mail -> {
                MailLauncher.sendMail(requireActivity(), getString(action.subject), getString(R.string.mail_body_translation))
            }
            is Announcement.Action.OpenScreen -> {
                ActivityNavigator.navigateTo(context, action.destination)
            }
        }
    }

    private fun closeModalAndDismissAnnouncement(announcement: Announcement) {
        viewModel.markAnnouncementAsSeen(announcement)
        closeModal()
    }

    private fun closeModal() {
        parentFragmentManager.popBackStack()
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(): AnnouncementFragment {
            return AnnouncementFragment()
        }
    }
}