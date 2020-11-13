package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import at.shockbytes.dante.R
import at.shockbytes.dante.announcement.Announcement
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.ui.viewmodel.AnnouncementViewModel
import at.shockbytes.dante.util.MailLauncher
import at.shockbytes.dante.util.UrlLauncher
import at.shockbytes.dante.util.setVisible
import at.shockbytes.dante.util.viewModelOfActivity
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.fragment_announcement.*
import kotlinx.android.synthetic.main.fragment_book_action_sheet.view.*
import javax.inject.Inject

class AnnouncementFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_announcement

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
            tv_announcement_title.setText(titleRes)
            tv_announcement_description.setText(descriptionRes)

            when (illustration) {
                is Announcement.Illustration.LottieIllustration -> {
                    iv_announcement.setVisible(false)
                    lottie_announcement.apply {
                        setVisible(true)
                        repeatCount = LottieDrawable.INFINITE
                        setAnimation(illustration.lottieRes)
                    }
                }
                is Announcement.Illustration.ImageIllustration -> {
                    lottie_announcement.setVisible(false, invisibilityState = View.INVISIBLE)
                    iv_announcement.apply {
                        setVisible(true)
                        setImageResource(illustration.drawableRes)
                    }
                }
            }

            btn_announcement_action.apply {
                setVisible(hasAction)

                this@with.action?.let { action ->
                    setOnClickListener {
                        performAnnouncementAction(action)
                    }
                    setText(action.actionLabel)
                }
            }

            layout_announcement.setOnClickListener {
                closeModalAndDismissAnnouncement(this)
            }
            btn_announcement_close.setOnClickListener {
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