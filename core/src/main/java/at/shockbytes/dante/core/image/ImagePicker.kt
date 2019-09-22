package at.shockbytes.dante.core.image

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import io.reactivex.Observable

interface ImagePicker {

    fun openGallery(activity: androidx.fragment.app.FragmentActivity): Observable<Uri>

    fun openCamera(activity: androidx.fragment.app.FragmentActivity): Observable<Uri>
}