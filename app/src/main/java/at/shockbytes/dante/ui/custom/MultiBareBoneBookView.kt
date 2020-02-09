package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import kotlinx.android.synthetic.main.multi_bare_bone_book_view.view.*

class MultiBareBoneBookView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.multi_bare_bone_book_view, this)
    }

    fun setTitle(title: String) {
        tv_multi_bare_bone_book_view.text = title
    }

    fun setMultipleBookImages(urls: List<String?>, imageLoader: ImageLoader) {

    }
}