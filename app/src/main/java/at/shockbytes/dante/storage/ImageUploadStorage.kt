package at.shockbytes.dante.storage

import android.net.Uri
import io.reactivex.Single

interface ImageUploadStorage {

    fun upload(image: Uri, progressListener: ((Int) -> Unit)? = null): Single<Uri>
}