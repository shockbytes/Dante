package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.app.ActivityOptionsCompat
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.navigation.ActivityNavigator
import at.shockbytes.dante.navigation.Destination
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.core.image.GlideImageLoader.loadRoundedBitmap
import at.shockbytes.dante.util.viewModelOf
import kotlinx.android.synthetic.main.bottom_sheet_menu.*
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class MenuFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    override fun getTheme() = R.style.BottomSheetDialogTheme

    private val viewModel: MainViewModel by lazy { viewModelOf(vmFactory) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as DanteApp).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_menu, container, false)
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    private fun bindViewModel() {
        viewModel.getUserEvent().observe(this, Observer(::handleUserEvent))
    }

    private fun handleUserEvent(event: MainViewModel.UserEvent) {

        when (event) {

            is MainViewModel.UserEvent.SuccessEvent -> {

                if (event.user != null) {
                    txtMenuUserName.text = event.user.displayName
                    txtMenuUserMail.text = event.user.email
                    btnMenuLogin.text = getString(R.string.logout)

                    event.user.photoUrl?.loadRoundedBitmap(requireContext())?.subscribe({ image ->
                        imageViewMenuUser.setImageBitmap(image)
                    }, { throwable ->
                        throwable.printStackTrace()
                    })
                } else {
                    txtMenuUserName.text = getString(R.string.anonymous_user)
                    txtMenuUserMail.text = ""
                    btnMenuLogin.text = getString(R.string.login)
                    imageViewMenuUser.setImageResource(R.drawable.ic_user_template_dark)
                }
            }

            is MainViewModel.UserEvent.LoginEvent -> {
                GoogleSignInDialogFragment.newInstance()
                    .setSignInListener {
                        requireActivity().startActivityForResult(event.signInIntent, DanteUtils.RC_SIGN_IN)
                    }
                    .setMaybeLaterListener { viewModel.signInMaybeLater(true) }
                    .show(childFragmentManager, "sign-in-fragment")
            }
            is MainViewModel.UserEvent.ErrorEvent -> {
                Toast.makeText(context, event.errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupViews() {

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