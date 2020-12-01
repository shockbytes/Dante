package at.shockbytes.dante.storage

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import io.reactivex.Single
import io.reactivex.SingleEmitter
import java.util.UUID

class FirebaseImageUploadStorage : ImageUploadStorage {

    private val imageRef = Firebase.storage.getReference("/custom_images")

    private var _backupUid: String? = null
    private val randomSessionUid: String
        get() = if (_backupUid == null) {
            "anonymous/${UUID.randomUUID()}".also { _backupUid = it }
        } else _backupUid!!

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: randomSessionUid

    private val defaultException = IllegalStateException("Unable to upload image! No reason given!")

    override fun upload(image: Uri, progressListener: ((Int) -> Unit)?): Single<Uri> {
        return Single.create { emitter ->

            val ref = imageRef.child("/$uid/${image.lastPathSegment}")

            ref.putFile(image)
                .addOnProgressListener { task ->
                    progressListener?.invoke(task.progress)
                }
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        emitter.tryOnErrorOrDefault(task.exception, defaultException)
                    }
                    ref.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emitter.onSuccess(task.result)
                    } else {
                        emitter.tryOnErrorOrDefault(task.exception, defaultException)
                    }
                }
        }
    }

    private val UploadTask.TaskSnapshot.progress: Int
        get() = ((100.0 * bytesTransferred) / totalByteCount).toInt()

    private fun <T> SingleEmitter<T>.tryOnErrorOrDefault(
        exception: Exception?,
        defaultException: Exception
    ) {
        tryOnError(exception ?: defaultException)
    }
}
