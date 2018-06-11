package at.shockbytes.dante.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import at.shockbytes.dante.R
import at.shockbytes.dante.signin.DanteUser
import at.shockbytes.dante.ui.activity.BackupActivity
import at.shockbytes.dante.ui.activity.SettingsActivity
import at.shockbytes.dante.ui.fragment.dialog.StatsDialogFragment
import at.shockbytes.dante.util.loadRoundedBitmap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @author  Martin Macheiner
 * Date:    06-Jun-18.
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

    private var user: DanteUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        user = arguments?.getParcelable(ARG_USER)
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

        view.findViewById<View>(R.id.btnMenuStatistics)?.setOnClickListener {
            StatsDialogFragment.newInstance().show(fragmentManager, "stats-dialog-fragment")
            dismiss()
        }

        view.findViewById<View>(R.id.btnMenuSortStrategy)?.setOnClickListener {
            Toast.makeText(context!!, "Show Sort Strategy dialog...", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        view.findViewById<View>(R.id.btnMenuBackup)?.setOnClickListener {
            activity?.let { act ->
                act.startActivityForResult(BackupActivity.newIntent(act),
                        BackupActivity.rcBackupRestored,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(act).toBundle())

            }
            dismiss()
        }

        view.findViewById<View>(R.id.btnMenuLogin)?.setOnClickListener {
            dismiss()
        }

        view.findViewById<View>(R.id.btnMenuSettings)?.setOnClickListener {
            activity?.let { act ->
                startActivity(SettingsActivity.newIntent(act),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(act).toBundle())
            }
            dismiss()
        }

        user?.let {

            view.findViewById<TextView>(R.id.txtMenuUserName)?.text = it.displayName
            view.findViewById<TextView>(R.id.txtMenuUserMail)?.text = it.email

            context?.let {  ctx ->
                it.photoUrl?.loadRoundedBitmap(ctx)?.subscribe ({ image ->
                    view.findViewById<ImageView>(R.id.imageViewMenuUser)?.setImageBitmap(image)
                }, {
                    it.printStackTrace()
                })
            }
        }

    }


    companion object {

        private const val ARG_USER = "arg_user"

        fun newInstance(user: DanteUser?): MenuFragment {
            val fragment = MenuFragment()
            val args = Bundle()
            args.putParcelable(ARG_USER, user)
            fragment.arguments = args
            return fragment
        }

    }

}