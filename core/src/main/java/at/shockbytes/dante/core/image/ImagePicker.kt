package at.shockbytes.dante.core.image

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import io.reactivex.Observable

interface ImagePicker {

    fun openGallery(activity: FragmentActivity): Observable<Uri>

    fun openCamera(activity: FragmentActivity): Observable<Uri>
}