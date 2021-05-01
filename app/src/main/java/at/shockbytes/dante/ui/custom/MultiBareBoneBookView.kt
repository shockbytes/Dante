package at.shockbytes.dante.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import at.shockbytes.dante.R
import at.shockbytes.dante.core.image.ImageLoader
import at.shockbytes.dante.databinding.MultiBareBoneBookViewBinding
import at.shockbytes.dante.util.layoutInflater
import at.shockbytes.util.AppUtils

class MultiBareBoneBookView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val vb: MultiBareBoneBookViewBinding
        get() = MultiBareBoneBookViewBinding.inflate(context.layoutInflater(), this, true)

    private val booksToDisplay = 8

    fun setTitle(title: String) {
        vb.tvMultiBareBoneBookView.text = title
    }

    fun setMultipleBookImages(urls: List<String?>, imageLoader: ImageLoader) {
        vb.containerMultiBareBoneBookView.removeAllViews()

        urls
            .mapIndexedNotNull { _, url ->
                if (!url.isNullOrEmpty()) {
                    createImageView().apply {
                        imageLoader.loadImageWithCornerRadius(
                                context = context,
                                url = url,
                                target = this,
                                cornerDimension = context.resources.getDimension(R.dimen.thumbnail_rounded_corner).toInt()
                        )
                    }
                } else null
            }
            .take(booksToDisplay)
            .forEach(vb.containerMultiBareBoneBookView::addView)
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