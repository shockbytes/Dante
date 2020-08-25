package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.util.AppUtils
import kotlinx.android.synthetic.main.multi_bare_bone_book_view.view.*

class MultiBareBoneBookView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val booksToDisplay = 8

    init {
        inflate(context, R.layout.multi_bare_bone_book_view, this)
    }

    fun setTitle(title: String) {
        tv_multi_bare_bone_book_view.text = title
    }

    fun setMultipleBookImages(urls: List<String?>, imageLoader: ImageLoader) {
        container_multi_bare_bone_book_view.removeAllViews()

        urls
            .mapIndexedNotNull { _,  url ->
                url?.let {
                    createImageView().apply {
                        imageLoader.loadImageWithCornerRadius(
                            context = context,
                            url = url,
                            target = this,
                            cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                        )
                    }
                }
            }
            .take(booksToDisplay)
            .forEach(container_multi_bare_bone_book_view::addView)
    }

    private fun createImageView(): ImageView {
        return ImageView(context).apply {
            layoutParams = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                height = AppUtils.convertDpInPixel(72, context)
                marginStart = AppUtils.convertDpInPixel(-16, context)
            }
            scaleType = ImageView.ScaleType.FIT_XY
        }
    }
}