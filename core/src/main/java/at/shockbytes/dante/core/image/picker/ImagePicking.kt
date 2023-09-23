package at.shockbytes.dante.core.image.picker

import androidx.fragment.app.Fragment

interface ImagePicking {

    fun openGallery(fragment: Fragment, requestCode: Int)
}