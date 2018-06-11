package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.shockbytes.dante.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotterknifex.bindView

/**
 * @author Martin Macheiner
 * Date: 01.01.2018.
 */

class GoogleWelcomeScreenDialogFragment : DialogFragment() {

    private val txtName: TextView by bindView(R.id.dialogfragment_google_welcome_txt_headline)
    private val imgView: ImageView by bindView(R.id.dialogfragment_google_welcome_imgview)

    private val welcomeView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_google_welcome, null, false)

    private var name: String? = null
    private var photoUrlString: String? = null

    private var listener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments?.getString(argName)
        photoUrlString = arguments?.getString(argPhotoUrl)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setTitle(R.string.welcome)
                .setIcon(R.drawable.ic_google)
                .setView(welcomeView)
                .setPositiveButton(getString(R.string.welcome_acknowledge)) { _, _ ->
                    listener?.invoke()
                }
                .create()
    }

    override fun onResume() {
        super.onResume()
        setupViews()
    }

    fun setOnAcknowledgedListener(listener: () -> Unit): GoogleWelcomeScreenDialogFragment {
        this.listener = listener
        return this
    }

    private fun setupViews() {

        val str = if (name != null) getString(R.string.welcome_with_name, name) else getString(R.string.login_title)
        txtName.text = str

        if (photoUrlString != null) {
            Picasso.with(context)
                    .load(Uri.parse(photoUrlString))
                    .transform(CropCircleTransformation())
                    .into(imgView)
        }
    }

    companion object {

        private const val argName = "arg_name"
        private const val argPhotoUrl = "arg_photo_url"

        fun newInstance(name: String?, photoUrl: Uri?): GoogleWelcomeScreenDialogFragment {
            val fragment = GoogleWelcomeScreenDialogFragment()
            val args = Bundle()
            args.putString(argName, name)
            args.putString(argPhotoUrl, photoUrl?.toString())
            fragment.arguments = args
            return fragment
        }
    }

}