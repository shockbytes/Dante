package at.shockbytes.dante.ui.image

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import io.reactivex.Observable
import com.qingmei2.rximagepicker.core.RxImagePicker

class RxDanteImagePicker : ImagePicker {

    override fun openGallery(activity: androidx.fragment.app.FragmentActivity): Observable<Uri> {
        return RxImagePicker
                .create(RxImagePickerDefinition::class.java)
                .openGallery(activity)
                .map { it.uri }
    }

    override fun openCamera(activity: androidx.fragment.app.FragmentActivity): Observable<Uri> {
        return RxImagePicker
                .create(RxImagePickerDefinition::class.java)
                .openCamera(activity)
                .map { it.uri }
    }
}