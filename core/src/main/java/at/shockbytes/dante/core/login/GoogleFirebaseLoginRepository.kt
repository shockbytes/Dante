package at.shockbytes.dante.core.login

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.lang.Exception

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 *
 * If migrating to firebase, use this docs
 * https://firebase.google.com/docs/auth/android/google-signin
 */
class GoogleFirebaseLoginRepository(
    private val client: GoogleSignInClient,
    private val context: Context,
    private val schedulers: SchedulerFacade
) : LoginRepository {

    private val fbAuth = FirebaseAuth.getInstance()

    private val signInSubject: BehaviorSubject<UserState> = BehaviorSubject.create()

    override val signInIntent: Intent
        get() = client.signInIntent

    init {
        postInitialSignInState()
    }

    private fun postInitialSignInState() {
        val state = try {
            getAccount().blockingGet()
        } catch (throwable: Exception) {
            Timber.e(throwable)
            UserState.Unauthenticated
        }
        signInSubject.onNext(state)
    }

    override fun signInWithGoogle(data: Intent): Single<DanteUser> {
        return Single
            .fromCallable {
                Tasks
                    .await(GoogleSignIn.getSignedInAccountFromIntent(data))
                    .authenticateToFirebase()
                    ?: throw LoginException("Cannot sign into Google Account! DanteUser = null")
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
            .doOnSuccess { user ->
                signInSubject.onNext(UserState.SignedInUser(user))
            }
    }

    override fun signInWithMail(mailAddress: String, password: String): Completable {
        TODO("Not yet implemented")
    }

    override fun signInAnonymously(): Completable {
        TODO("Not yet implemented")
    }

    private fun GoogleSignInAccount.authenticateToFirebase(): DanteUser? {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return Tasks.await(fbAuth.signInWithCredential(credential))?.let { authResult ->

            val givenName = authResult.additionalUserInfo?.profile?.get("given_name") as? String
            authResult.user?.toDanteUser(givenName)
        }
    }

    override fun signOut(): Completable {
        return Completable
            .fromAction(fbAuth::signOut)
            .doOnComplete {
                signInSubject.onNext(UserState.Unauthenticated)
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun observeAccount(): Observable<UserState> {
        return signInSubject
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun getAccount(): Single<UserState> {
        return Single
            .fromCallable {
                fbAuth.currentUser?.toDanteUser()
                    ?.let(UserState::SignedInUser)
                    ?: UserState.Unauthenticated
            }
            .subscribeOn(schedulers.io)
    }

    override fun getAuthorizationHeader(): Single<String> {
        return getAccount().map { acc ->
            val authToken = if (acc is UserState.SignedInUser) acc.user.authToken ?: "" else ""
            getAuthorizationHeader(authToken)
        }
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return if (getAccount().blockingGet() is UserState.SignedInUser) {
            GoogleSignIn.getLastSignedInAccount(context)
        } else {
            null
        }
    }

    private fun FirebaseUser.toDanteUser(givenName: String? = this.displayName): DanteUser {
        return DanteUser(
            givenName,
            this.displayName,
            this.email,
            this.photoUrl,
            this.providerId,
            Tasks.await(this.getIdToken(false))?.token,
            this.uid
        )
    }
}