package at.shockbytes.dante.ui.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.app.ActivityOptionsCompat
import android.view.View
import android.view.ViewGroup
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

    private val viewModel: MainViewModel by lazy { viewModelOf<MainViewModel>(vmFactory) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as DanteApp).appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        viewModel.getUserEvent().observe(this, Observer { event ->

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
                            requireActivity().startActivityForResult(event.signInIntent, DanteUtils.rcSignIn)
                        }
                        .setMaybeLaterListener { viewModel.signInMaybeLater(true) }
                        .show(childFragmentManager, "sign-in-fragment")
                }
            }
        })
    }

    private fun setupViews() {

        val sceneTransition = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()

        btnMenuStatistics.setOnClickListener {
            ActivityNavigator.navigateTo(
                activity,
                Destination.Statistics,
                sceneTransition
            )
            dismiss()
        }

        btnMenuTimeline.setOnClickListener {
            ActivityNavigator.navigateTo(
                activity,
                Destination.Timeline,
                sceneTransition
            )
            dismiss()
        }

        btnMenuBookStorage.setOnClickListener {
            ActivityNavigator.navigateTo(
                activity,
                Destination.BookStorage,
                sceneTransition
            )
            dismiss()
        }

        btnMenuLogin.setOnClickListener {
            viewModel.loginLogout()
        }

        btnMenuSettings.setOnClickListener {
            ActivityNavigator.navigateTo(
                activity,
                Destination.Settings,
                sceneTransition
            )
            dismiss()
        }
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