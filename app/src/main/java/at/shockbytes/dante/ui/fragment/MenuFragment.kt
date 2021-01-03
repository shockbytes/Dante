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
import at.shockbytes.dante.ui.fragment.dialog.SimpleInputDialogFragment
import at.shockbytes.dante.ui.viewmodel.MailLoginViewModel
import at.shockbytes.dante.ui.viewmodel.UserViewModel
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOfActivity
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
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

    private val userViewModel: UserViewModel by lazy {
        viewModelOfActivity(requireActivity(), vmFactory)
    }

    override fun bindViewModel() {
        userViewModel.getUserViewState().observe(this, Observer(::handleUserViewState))

        userViewModel.onUserEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleUserEvent)
            .addTo(compositeDisposable)
    }

    private fun handleUserEvent(event: UserViewModel.UserEvent) {
        when (event) {
            is UserViewModel.UserEvent.Login -> navigateToLogin()
            is UserViewModel.UserEvent.AnonymousLogout -> showAnonymousLogout()
            is UserViewModel.UserEvent.AnonymousUpgradeFailed -> showAnonymousUpgradeFailed(event.message)
        }
    }

    private fun navigateToLogin() {
        val sceneTransition = requireActivity()
            .let(ActivityOptionsCompat::makeSceneTransitionAnimation)
            .toBundle()
        ActivityNavigator.navigateTo(context, Destination.Login, sceneTransition)
    }

    private fun showAnonymousLogout() {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_incognito)
            title(text = getString(R.string.logout_incognito))
            message(text = getString(R.string.logout_incognito_hint))
            positiveButton(R.string.logout) {
                userViewModel.forceLogout()
                dismiss()
            }
            negativeButton(R.string.cancel) {
                dismiss()
            }
            cancelOnTouchOutside(true)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun showAnonymousUpgradeFailed(message: String?) {
        val snackBarMessage = message ?: getString(R.string.anonymous_upgrade_error)
        Snackbar.make(requireView(), snackBarMessage, Snackbar.LENGTH_LONG).show()
    }

    override fun unbindViewModel() = Unit

    private fun handleUserViewState(event: UserViewModel.UserViewState) {

        when (event) {

            is UserViewModel.UserViewState.LoggedIn -> {
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

            is UserViewModel.UserViewState.UnauthenticatedUser -> {
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
            userViewModel.loginLogout()
        }

        btnMenuSettings.setOnClickListener {
            navigateToAndDismiss(Destination.Settings)
        }

        profileActionViewMenu.onActionButtonClicked()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleProfileClick)
            .addTo(compositeDisposable)
    }

    private fun handleProfileClick(profileActionViewClick: ProfileActionViewClick) {
        when (profileActionViewClick) {
            ProfileActionViewClick.UPGRADE_ANONYMOUS_ACCOUNT -> showUpgradeBottomSheet()
            ProfileActionViewClick.CHANGE_NAME -> showChangeNameScreen()
            ProfileActionViewClick.CHANGE_IMAGE -> userViewModel.changeUserImage(requireActivity())
            ProfileActionViewClick.CHANGE_PASSWORD -> {
                // TODO Show update password screen, maybe reuse simple input
            }
        }
    }

    private fun showChangeNameScreen() {
        SimpleInputDialogFragment
            .newInstance(
                title = R.string.account_change_name_title,
                icon = R.drawable.ic_user_template_dark,
                message = R.string.account_change_name_message,
                hint = R.string.account_change_name_hint,
                positiveButtonText = android.R.string.ok
            )
            .setOnInputEnteredListener(userViewModel::changeUserName)
            .show(parentFragmentManager, "query-dialog-fragment")
    }

    private fun showUpgradeBottomSheet() {
        MailLoginBottomSheetDialogFragment
            .newInstance(MailLoginViewModel.MailLoginState.ShowEmailAndPassword(isSignUp = true, R.string.anonymous_upgrade))
            .setOnCredentialsEnteredListener(userViewModel::anonymousUpgrade)
            .show(parentFragmentManager, "anonymous-upgrade-fragment")
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

        ActivityNavigator.navigateTo(
            activity,
            destination,
            ActivityOptionsCompat
                .makeSceneTransitionAnimation(requireActivity())
                .toBundle()
        )

        dismiss()
    }

    companion object {

        fun newInstance() = MenuFragment()
    }
}