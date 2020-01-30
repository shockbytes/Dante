package at.shockbytes.dante.ui.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import at.shockbytes.dante.R
import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.injection.AppComponent

class CreateLabelDialogFragment : InteractiveViewDialogFragment<BookLabel>() {

    override val containerView: View
        get() = LayoutInflater.from(context).inflate(R.layout.dialogfragment_create_label, null, false)

    override fun setupViews() {
    }

    override fun injectToGraph(appComponent: AppComponent) = Unit

    companion object {

        fun newInstance(): CreateLabelDialogFragment {
            return CreateLabelDialogFragment()
        }
    }
}