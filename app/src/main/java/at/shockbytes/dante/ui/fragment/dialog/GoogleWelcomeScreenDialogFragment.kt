package at.shockbytes.dante.ui.fragment.dialog

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.dante.R
import at.shockbytes.dante.dagger.AppComponent
import at.shockbytes.dante.ui.image.ImageLoader
import kotterknife.bindView
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 01.01.2018.
 */

class GoogleWelcomeScreenDialogFragment : BaseDialogFragment() {

    private val txtName: TextView by bindView(R.id.dialogfragment_google_welcome_txt_headline)
    private val imgView: ImageView by bindView(R.id.dialogfragment_google_welcome_imgview)

    private val welcomeView: View
        get() = LayoutInflater.from(context)
                .inflate(R.layout.dialogfragment_google_welcome, null, false)

    private var name: String? = null
    private var photoUrlString: String? = null

    private var listener: (() -> Unit)? = null

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments?.getString(argName)
        photoUrlString = arguments?.getString(argPhotoUrl)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setView(welcomeView)
                .setPositiveButton(getString(R.string.welcome_acknowledge)) { _, _ ->
                    listener?.invoke()
                }
                .create()
                .also { it.requestWindowFeature(Window.FEATURE_NO_TITLE) }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
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

        photoUrlString?.let { url ->
            context?.let { ctx ->
                imageLoader.loadImageUri(ctx, Uri.parse(url), imgView, circular = true)
            }
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