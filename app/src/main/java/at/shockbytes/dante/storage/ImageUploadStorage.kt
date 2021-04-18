package at.shockbytes.dante.storage

import android.net.Uri
import io.reactivex.rxjava3.core.Single

interface ImageUploadStorage {

    fun uploadCustomImage(image: Uri, progressListener: ((Int) -> Unit)? = null): Single<Uri>

    fun uploadUserImage(image: Uri): Single<Uri>
}