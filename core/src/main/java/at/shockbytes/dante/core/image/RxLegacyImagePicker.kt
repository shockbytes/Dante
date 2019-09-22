package at.shockbytes.dante.core.image

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import io.reactivex.Observable

class RxLegacyImagePicker : ImagePicker {

    override fun openGallery(activity: androidx.fragment.app.FragmentActivity): Observable<Uri> {
        return RxImagePicker.with(activity.fragmentManager).requestImage(Sources.GALLERY)
    }

    override fun openCamera(activity: androidx.fragment.app.FragmentActivity): Observable<Uri> {
        return RxImagePicker.with(activity.fragmentManager).requestImage(Sources.CAMERA)
    }
}