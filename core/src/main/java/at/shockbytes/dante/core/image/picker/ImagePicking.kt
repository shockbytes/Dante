package at.shockbytes.dante.core.image.picker

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import io.reactivex.rxjava3.core.Single

interface ImagePicking {

    fun openGallery(activity: FragmentActivity): Single<Uri>
}