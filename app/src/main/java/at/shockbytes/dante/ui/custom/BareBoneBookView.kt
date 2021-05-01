package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import at.shockbytes.dante.R
import at.shockbytes.dante.databinding.BareBoneBookViewBinding
import at.shockbytes.dante.util.layoutInflater

class BareBoneBookView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val vb = BareBoneBookViewBinding.inflate(context.layoutInflater(), this, true)

    val imageView: ImageView
        get() = vb.ivBareBoneBookView

    fun setTitle(title: String) {
        vb.tvBareBoneBookView.text = title
    }
}