package at.shockbytes.dante.core.image.picker

import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker

class DefaultImagePicking(private val imagePickerConfig: ImagePickerConfig) : ImagePicking {

    override fun openGallery(fragment: Fragment, requestCode: Int) {
        ImagePicker.with(fragment)
            .galleryOnly()
            .compress(imagePickerConfig.maxSize)
            .maxResultSize(imagePickerConfig.maxWidth, imagePickerConfig.maxHeight)
            .start(requestCode)
    }
}