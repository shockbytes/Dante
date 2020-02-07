package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import at.shockbytes.dante.R
import kotlinx.android.synthetic.main.bare_bone_book_view.view.*

class BareBoneBookView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val imageView: ImageView
        get() = iv_bare_bone_book_view

    init {
        inflate(context, R.layout.bare_bone_book_view, this)
    }

    fun setTitle(title: String) {
        tv_bare_bone_book_view.text = title
    }
}