package at.shockbytes.dante.core.image.picker

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import io.reactivex.Single

interface ImagePicking {

    fun openGallery(activity: FragmentActivity): Single<Uri>
}