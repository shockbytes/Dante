package at.shockbytes.dante.ui.fragment

import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import android.view.View
import androidx.lifecycle.Observer
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.GlideImageLoader.loadRoundedBitmap
import at.shockbytes.dante.injection.AppComponent
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.custom.profile.ProfileActionViewClick
import at.shockbytes.dante.ui.custom.profile.ProfileActionViewState
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOfActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.bottom_sheet_menu.*
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class MenuFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override val layoutRes: Int = R.layout.bottom_sheet_menu

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    private val viewModel: MainViewModel by lazy {
        viewModelOfActivity(requireActivity(), vmFactory)
    }

    override fun bindViewModel() {
        viewModel.getUserEvent().observe(this, Observer(::handleUserEvent))

        viewModel.onLoginEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val sceneTransition = requireActivity()
                    .let(ActivityOptionsCompat::makeSceneTransitionAnimation)
                    .toBundle()
                ActivityNavigator.navigateTo(context, Destination.Login, sceneTransition)
            }
            .addTo(compositeDisposable)
    }

    override fun unbindViewModel() = Unit

    private fun handleUserEvent(event: MainViewModel.UserEvent) {

        when (event) {

            is MainViewModel.UserEvent.LoggedIn -> {
                btnMenuLogin.text = getString(R.string.logout)

                profileHeaderMenu.setUser(event.user.displayName, event.user.email)
                profileActionViewMenu.setState(event.profileActionViewState)

                val photoUrl = event.user.photoUrl
                if (photoUrl != null) {
                    photoUrl.loadRoundedBitmap(requireContext())
                        .subscribe({ image ->
                            profileHeaderMenu.imageView.setImageBitmap(image)
                        }, { throwable ->
                            throwable.printStackTrace()
                        })
                } else {
                    profileHeaderMenu.imageView.setImageResource(R.drawable.ic_user_template_dark)
                }
            }

            is MainViewModel.UserEvent.UnauthenticatedUser -> {
                btnMenuLogin.text = getString(R.string.login)

                profileActionViewMenu.setState(ProfileActionViewState.Hidden)
                profileHeaderMenu.reset()
            }
        }
    }

    override fun setupViews() {

        btnMenuStatistics.setOnClickListener {
            navigateToAndDismiss(Destination.Statistics)
        }

        btnMenuTimeline.setOnClickListener {
            navigateToAndDismiss(Destination.Timeline)
        }

        btnMenuInspirations.setOnClickListener {
            navigateToAndDismiss(Destination.Inspirations)
        }

        btnMenuBookStorage.setOnClickListener {
            navigateToAndDismiss(Destination.BookStorage)
        }

        btnMenuLogin.setOnClickListener {
            viewModel.loginLogout()
        }

        btnMenuSettings.setOnClickListener {
            navigateToAndDismiss(Destination.Settings)
        }

        profileActionViewMenu.onActionButtonClicked()
            .subscribe(::handleProfileClick)
            .addTo(compositeDisposable)
    }

    private fun handleProfileClick(profileActionViewClick: ProfileActionViewClick) {
        when (profileActionViewClick) {
            ProfileActionViewClick.UPGRADE_ANONYMOUS_ACCOUNT -> {
                // TODO
            }
            ProfileActionViewClick.CHANGE_NAME -> {
                // TODO
            }
            ProfileActionViewClick.CHANGE_IMAGE -> Unit // Not implemented yet...
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun navigateToAndDismiss(destination: Destination) {

        val sceneTransition = ActivityOptionsCompat
            .makeSceneTransitionAnimation(requireActivity())
            .toBundle()

        ActivityNavigator.navigateTo(activity, destination, sceneTransition)

        dismiss()
    }

    companion object {

        fun newInstance(): MenuFragment {
            val fragment = MenuFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}