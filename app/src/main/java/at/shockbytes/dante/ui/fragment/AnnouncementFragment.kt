package at.shockbytes.dante.ui.fragment

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import at.shockbytes.dante.R
import at.shockbytes.dante.announcement.Announcement
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.viewmodel.AnnouncementViewModel
import javax.inject.Inject

class AnnouncementFragment : BaseFragment() {

    override val layoutId: Int = R.layout.fragment_announcement

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AnnouncementViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)[AnnouncementViewModel::class.java]
    }

    override fun setupViews() {
        // TODO
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestAnnouncementState()

        viewModel.getAnnouncementState().observe(this, Observer { announcementState ->
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
        // TODO
    }

    private fun closeModal() {
        fragmentManager?.popBackStack()
    }

    override fun unbindViewModel() = Unit

    companion object {

        fun newInstance(): AnnouncementFragment {
            return AnnouncementFragment()
        }
    }
}