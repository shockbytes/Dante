package at.shockbytes.dante.core.image.picker

import android.app.Activity
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import io.reactivex.Single

class DefaultImagePicking(private val imagePickerConfig: ImagePickerConfig) : ImagePicking {

    override fun openGallery(activity: FragmentActivity): Single<Uri> {
        return Single.create { emitter ->
            ImagePicker.with(activity)
                .galleryOnly()
                .compress(imagePickerConfig.maxSize)
                .maxResultSize(imagePickerConfig.maxWidth, imagePickerConfig.maxHeight)
                .start { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        val url = data?.data
                        if (url != null) {
                            emitter.onSuccess(url)
                        } else {
                            emitter.tryOnError(NullPointerException("Data url is null!"))
                        }
                    } else {
                        emitter.tryOnError(IllegalStateException("Cannot open gallery! Activity result canceled!"))
                    }
                }
        }
    }
}