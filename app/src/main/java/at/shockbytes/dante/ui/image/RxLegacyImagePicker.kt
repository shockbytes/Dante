package at.shockbytes.dante.ui.image

import android.net.Uri
import android.support.v4.app.FragmentActivity
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import io.reactivex.Observable

class RxLegacyImagePicker : ImagePicker {

    override fun openGallery(activity: FragmentActivity): Observable<Uri> {
        return RxImagePicker.with(activity.fragmentManager).requestImage(Sources.GALLERY)
    }

    override fun openCamera(activity: FragmentActivity): Observable<Uri> {
        return RxImagePicker.with(activity.fragmentManager).requestImage(Sources.CAMERA)
    }
}