package at.shockbytes.dante.ui.fragment

import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.InputType
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
import at.shockbytes.dante.ui.viewmodel.MailLoginViewModel
import at.shockbytes.dante.ui.viewmodel.UserViewModel
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.Login
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.AnonymousLogout
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.AnonymousUpgradeEvent.AnonymousUpgradeFailed
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.AnonymousUpgradeEvent.AnonymousUpgradeSuccess
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserNameEvent.UserNameUpdated
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserNameEvent.UserNameUpdateError
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserNameEvent.UserNameEmpty
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserNameEvent.UserNameTooLong
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserImageEvent.UserImageUpdated
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserImageEvent.UserImageUpdateError
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserPasswordEvent.UserPasswordUpdated
import at.shockbytes.dante.ui.viewmodel.UserViewModel.UserEvent.UserPasswordEvent.UserPasswordUpdateError
import at.shockbytes.dante.util.addTo
import at.shockbytes.dante.util.viewModelOfActivity
import at.shockbytes.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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

    private fun handleUserEvent(event: UserViewModel.UserEvent) {
        when (event) {
            is Login -> navigateToLogin()
            is AnonymousLogout -> showAnonymousLogout()
            is AnonymousUpgradeFailed -> showAnonymousUpgradeFailed(event.message)
            is AnonymousUpgradeSuccess -> showAnonymousUpgradeSuccess(event.mailAddress)
            is UserNameUpdated -> showUserNameUpdatedMessage()
            is UserNameUpdateError -> showUserNameUpdateError(event.message)
            is UserNameEmpty -> showUserNameEmptyError()
            is UserNameTooLong -> showUserNameTooLongError(event.maxAllowedLength)
            is UserImageUpdated -> showUserImageUpdatedMessage()
            is UserImageUpdateError -> showImageUpdateError(event.message)
            is UserPasswordUpdated -> showUserPasswordUpdatedMessage()
            is UserPasswordUpdateError -> showUserUpdateErrorMessage(event.message)
        }
    }

    private fun navigateToLogin() {

        // Navigate to the LoginActivity
        ActivityNavigator.navigateTo(
            context,
            Destination.Login,
            requireActivity()
                .let(ActivityOptionsCompat::makeSceneTransitionAnimation)
                .toBundle()
        )

        /*
         * The LoginActivity should not show the history of the previous MainActivity,
         * therefore dismiss the MenuFragment and the MainActivity,
         * before navigating to the LoginActivity
         */
        dismiss()
        activity?.finish()
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
        val toastMessage = message ?: getString(R.string.anonymous_upgrade_error)
        showToast(toastMessage)
    }

    private fun showAnonymousUpgradeSuccess(mailAddress: String) {
        showToast(getString(R.string.anonymous_upgrade_success, mailAddress))
    }

    private fun showUserNameUpdatedMessage() = showToast(R.string.user_name_updated)

    private fun showUserNameUpdateError(message: String?) {
        val toastMessage = message ?: getString(R.string.user_update_error)
        showToast(toastMessage)
    }

    private fun showUserNameEmptyError() = showToast(R.string.user_name_empty)

    private fun showUserNameTooLongError(maxAllowedLength: Int) {
        val message = getString(R.string.user_name_too_long, maxAllowedLength)
        showToast(message)
    }

    private fun showUserImageUpdatedMessage() = showToast(R.string.user_image_updated)

    private fun showImageUpdateError(message: String?) {
        val toastMessage = message ?: getString(R.string.user_update_error)
        showToast(toastMessage)
    }

    private fun showUserPasswordUpdatedMessage() = showToast(R.string.password_update_success)

    private fun showUserUpdateErrorMessage(message: String?) {
        val toastMessage = message ?: getString(R.string.password_update_error)
        showToast(toastMessage)
    }

    override fun unbindViewModel() = Unit

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
            ProfileActionViewClick.CHANGE_PASSWORD -> showChangePasswordScreen()
        }
    }

    private fun showChangeNameScreen() {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_user_template_dark)
            title(R.string.account_change_name_title)
            message(R.string.account_change_name_message)
            input(allowEmpty = false, hintRes = R.string.account_change_name_hint) { _, userName ->
                userViewModel.changeUserName(userName.toString())
            }
            positiveButton(R.string.change)
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(true)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
    }

    private fun showChangePasswordScreen() {
        MaterialDialog(requireContext()).show {
            icon(R.drawable.ic_password)
            title(R.string.account_change_password_title)
            message(R.string.account_change_password_message)
            input(
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                allowEmpty = false,
                hintRes = R.string.account_change_password_hint
            ) { _, password -> userViewModel.updatePassword(password.toString()) }
            positiveButton(R.string.change)
            negativeButton(R.string.cancel)
            cancelOnTouchOutside(true)
            cornerRadius(AppUtils.convertDpInPixel(6, requireContext()).toFloat())
        }
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