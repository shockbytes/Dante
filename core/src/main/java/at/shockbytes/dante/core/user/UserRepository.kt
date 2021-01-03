package at.shockbytes.dante.core.user

import android.net.Uri
import io.reactivex.Completable

interface UserRepository {

    fun updateUserName(userName: String): Completable

    fun updateUserImage(imageUri: Uri): Completable
}