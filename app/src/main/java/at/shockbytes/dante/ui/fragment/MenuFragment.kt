package at.shockbytes.dante.ui.fragment

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.DanteApp
import at.shockbytes.dante.R
import at.shockbytes.dante.ui.activity.core.ActivityNavigation
import at.shockbytes.dante.ui.fragment.dialog.GoogleSignInDialogFragment
import at.shockbytes.dante.ui.fragment.dialog.SortStrategyDialogFragment
import at.shockbytes.dante.ui.viewmodel.MainViewModel
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.ui.image.GlideImageLoader.loadRoundedBitmap
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    06.06.2018
 */
class MenuFragment : BottomSheetDialogFragment() {

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as DanteApp).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.bottom_sheet_menu, null)
        dialog.setContentView(contentView)
        (contentView.parent as View)
                .setBackgroundColor(ContextCompat.getColor(context!!, android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(bottomSheetBehaviorCallback)
        }

        setupViews(contentView)
    }

    private fun setupViews(view: View) {

        val sceneTransition = activity?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(it)
        }?.toBundle()

        view.findViewById<View>(R.id.btnMenuStatistics)?.setOnClickListener {
            ActivityNavigation.navigateTo(
                    activity,
                    ActivityNavigation.Destination.Statistics,
                    sceneTransition
            )
            dismiss()
        }

        view.findViewById<View>(R.id.btnMenuSortStrategy)?.setOnClickListener {
            SortStrategyDialogFragment.newInstance()
                    .setOnApplyListener {
                        dismiss()
                    }
                    .show(fragmentManager, "sort-dialog-fragment")
        }

        view.findViewById<View>(R.id.btnMenuBackup)?.setOnClickListener {
            ActivityNavigation.navigateTo(
                    activity,
                    ActivityNavigation.Destination.Backup,
                    sceneTransition
            )
            dismiss()
        }

        view.findViewById<View>(R.id.btnMenuLogin)?.setOnClickListener {
            viewModel.loginLogout()
        }

        view.findViewById<View>(R.id.btnMenuSettings)?.setOnClickListener {
            ActivityNavigation.navigateTo(
                    activity,
                    ActivityNavigation.Destination.Settings,
                    sceneTransition
            )
            dismiss()
        }

        viewModel.userEvent.observe(this, Observer { event ->

            when (event) {

                is MainViewModel.UserEvent.SuccessEvent -> {

                    if (event.user != null) {
                        view.findViewById<TextView>(R.id.txtMenuUserName)?.text = event.user.displayName
                        view.findViewById<TextView>(R.id.txtMenuUserMail)?.text = event.user.email
                        view.findViewById<View>(R.id.btnMenuBackup)?.visibility = View.VISIBLE
                        view.findViewById<Button>(R.id.btnMenuLogin)?.text = getString(R.string.logout)

                        context?.let { ctx ->
                            event.user.photoUrl?.loadRoundedBitmap(ctx)?.subscribe({ image ->
                                view.findViewById<ImageView>(R.id.imageViewMenuUser)?.setImageBitmap(image)
                            }, {
                                it.printStackTrace()
                            })
                        }
                    } else {
                        view.findViewById<TextView>(R.id.txtMenuUserName)?.text = getString(R.string.anonymous_user)
                        view.findViewById<TextView>(R.id.txtMenuUserMail)?.text = ""
                        view.findViewById<View>(R.id.btnMenuBackup)?.visibility = View.GONE
                        view.findViewById<Button>(R.id.btnMenuLogin)?.text = getString(R.string.login)
                        view.findViewById<ImageView>(R.id.imageViewMenuUser)?.setImageResource(R.drawable.ic_user_template_dark)
                    }
                }

                is MainViewModel.UserEvent.LoginEvent -> {
                    GoogleSignInDialogFragment.newInstance()
                            .setSignInListener {
                                activity?.startActivityForResult(event.signInIntent, DanteUtils.rcSignIn)
                            }
                            .setMaybeLaterListener { viewModel.signInMaybeLater(true) }
                            .show(fragmentManager, "sign-in-fragment")
                }
            }
        })
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