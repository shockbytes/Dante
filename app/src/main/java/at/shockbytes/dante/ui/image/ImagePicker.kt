package at.shockbytes.dante.ui.image

import android.net.Uri
import android.support.v4.app.FragmentActivity
import io.reactivex.Observable

interface ImagePicker {

    fun openGallery(activity: FragmentActivity): Observable<Uri>

    fun openCamera(activity: FragmentActivity): Observable<Uri>
}