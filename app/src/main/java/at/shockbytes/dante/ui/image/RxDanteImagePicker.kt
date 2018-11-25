package at.shockbytes.dante.ui.image

import android.net.Uri
import android.support.v4.app.FragmentActivity
import io.reactivex.Observable
import com.qingmei2.rximagepicker.core.RxImagePicker

class RxDanteImagePicker : ImagePicker {

    override fun openGallery(activity: FragmentActivity): Observable<Uri> {
        return RxImagePicker
                .create(RxImagePickerDefinition::class.java)
                .openGallery(activity)
                .map { it.uri }
    }

    override fun openCamera(activity: FragmentActivity): Observable<Uri> {
        return RxImagePicker
                .create(RxImagePickerDefinition::class.java)
                .openCamera(activity)
                .map { it.uri }
    }
}