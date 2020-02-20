package at.shockbytes.dante.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import android.widget.ImageView
import at.shockbytes.dante.util.DanteUtils
import at.shockbytes.dante.util.DanteUtils.checkUrlForHttps
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * This class is an object because otherwise Kotlin cannot resolve the extension functions
 * of [android.net.Uri].
 */
object GlideImageLoader : ImageLoader {

    override fun loadImage(
        context: Context,
        url: String,
        target: ImageView,
        @DrawableRes placeholder: Int,
        circular: Boolean,
        callback: ImageLoadingCallback?,
        callbackHandleValues: Pair<Boolean, Boolean>?
    ) {
        val request = Glide.with(context).load(url.checkUrlForHttps())
            .apply(getRequestOptions(context, circular, placeholder))
        executeRequest(request, target, callback, callbackHandleValues)
    }

    override fun loadImageResource(
        context: Context,
        @DrawableRes resource: Int,
        target: ImageView,
        @DrawableRes placeholder: Int,
        circular: Boolean,
        callback: ImageLoadingCallback?,
        callbackHandleValues: Pair<Boolean, Boolean>?
    ) {
        val request = Glide.with(context).load(resource)
            .apply(getRequestOptions(context, circular, placeholder))
        executeRequest(request, target, callback, callbackHandleValues)
    }

    override fun loadImageUri(
        context: Context,
        uri: Uri,
        target: ImageView,
        placeholder: Int,
        circular: Boolean,
        callback: ImageLoadingCallback?,
        callbackHandleValues: Pair<Boolean, Boolean>?
    ) {
        val request = Glide.with(context).load(uri)
            .apply(getRequestOptions(context, circular, placeholder))
        executeRequest(request, target, callback, callbackHandleValues)
    }

    override fun loadImageWithCornerRadius(
        context: Context,
        url: String,
        target: ImageView,
        @DrawableRes placeholder: Int,
        @Dimension cornerDimension: Int,
        callback: ImageLoadingCallback?,
        callbackHandleValues: Pair<Boolean, Boolean>?
    ) {
        val request = Glide
            .with(context)
            .load(url.checkUrlForHttps())
            .apply(
                RequestOptions()
                    .placeholder(DanteUtils.vector2Drawable(context, placeholder))
                    .transform(CenterInside(), RoundedCorners(cornerDimension))
            )

        executeRequest(request, target, callback, callbackHandleValues)
    }

    override fun Uri.loadBitmap(context: Context): Single<Bitmap> {
        return Single
            .fromCallable {
                (Glide.with(context).load(this).submit().get() as BitmapDrawable).bitmap
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    override fun Uri.loadRoundedBitmap(context: Context): Single<Bitmap> {
        return Single
            .fromCallable {
                (Glide.with(context).load(this)
                    .apply(RequestOptions.circleCropTransform()).submit().get() as BitmapDrawable).bitmap
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
    }

    // --------------------------------------------------------------------------------------------

    private fun executeRequest(
        requestCopy: RequestBuilder<Drawable>,
        target: ImageView,
        callback: ImageLoadingCallback?,
        callbackHandleValues: Pair<Boolean, Boolean>?
    ) {
        var request = requestCopy
        if (callback != null && callbackHandleValues != null) {
            val (handleReady, handleError) = callbackHandleValues

            request = request.listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    callback.onImageLoadingFailed(e)
                    return handleError
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    callback.onImageResourceReady(resource)
                    return handleReady
                }
            })
        }
        request.into(target)
    }

    private fun getRequestOptions(
        context: Context,
        isCircular: Boolean,
        placeholder: Int
    ): RequestOptions {
        var options = RequestOptions()

        if (isCircular) {
            options = RequestOptions.circleCropTransform()
        }
        if (placeholder > 0) {
            options = options.placeholder(DanteUtils.vector2Drawable(context, placeholder))
        }
        return options
    }
}