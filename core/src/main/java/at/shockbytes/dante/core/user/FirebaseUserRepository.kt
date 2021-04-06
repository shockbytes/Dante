package at.shockbytes.dante.core.user

import android.net.Uri
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import io.reactivex.Completable

class FirebaseUserRepository(
    private val fbAuth: FirebaseAuth,
    private val schedulers: SchedulerFacade
) : UserRepository {

    override fun updateUserName(userName: String): Completable {

        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        return updateUserProfile(changeRequest)
    }

    override fun updateUserImage(imageUri: Uri): Completable {

        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(imageUri)
            .build()

        return updateUserProfile(changeRequest)
    }

    private fun updateUserProfile(changeRequest: UserProfileChangeRequest): Completable {
        return Completable
            .create { emitter ->

                val currentUser = fbAuth.currentUser
                if (currentUser == null) {
                    emitter.tryOnError(NullPointerException("User is not logged in!"))
                } else {

                    currentUser.updateProfile(changeRequest).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            emitter.onComplete()
                        } else {
                            val exception = task.exception
                                ?: IllegalStateException("Unknown update user name error")
                            emitter.tryOnError(exception)
                        }
                    }
                }
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.io)
    }
}