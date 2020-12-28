package at.shockbytes.dante.core.login

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.core.fromSingleToCompletable
import at.shockbytes.dante.util.completableOf
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.singleOf
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import kotlin.Exception

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 *
 * If migrating to firebase, use this docs
 * https://firebase.google.com/docs/auth/android/google-signin
 */
class GoogleFirebaseLoginRepository(
    client: GoogleSignInClient,
    private val context: Context,
    private val schedulers: SchedulerFacade
) : LoginRepository {

    private val fbAuth = FirebaseAuth.getInstance()

    private val signInSubject: BehaviorSubject<UserState> = BehaviorSubject.create()

    override val googleLoginIntent: Intent = client.signInIntent

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

    override fun loginWithGoogle(data: Intent): Completable {
        return login(errorMessage = "Cannot sign into Google Account! DanteUser = null") {
            Tasks.await(GoogleSignIn.getSignedInAccountFromIntent(data)).authenticateToFirebase()
        }
    }

    private fun GoogleSignInAccount.authenticateToFirebase(): DanteUser? {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return Tasks.await(fbAuth.signInWithCredential(credential))?.let { authResult ->

            val givenName = authResult.additionalUserInfo?.profile?.get("given_name") as? String
            authResult.user?.toDanteUser(givenName)
        }
    }

    override fun fetchSignInMethodsForEmail(mailAddress: String): Single<List<String>> {
        return singleOf {
            Tasks.await(fbAuth.fetchSignInMethodsForEmail(mailAddress)).signInMethods ?: listOf()
        }
    }

    override fun createAccountWithMail(mailAddress: String, password: String): Completable {
        return login(errorMessage = "Problem with mail account creation") {
            Tasks.await(fbAuth.createUserWithEmailAndPassword(mailAddress, password)).user?.toDanteUser()
        }
    }

    override fun loginWithMail(mailAddress: String, password: String): Completable {
        return login(errorMessage = "Problem with mail account login") {
            Tasks.await(fbAuth.signInWithEmailAndPassword(mailAddress, password)).user?.toDanteUser()
        }
    }

    override fun loginAnonymously(): Completable {
        return login(errorMessage = "Cannot anonymously sign into Firebase! AuthResult = null") {
            Tasks.await(fbAuth.signInAnonymously()).user?.toDanteUser()
        }
    }

    private fun login(errorMessage: String, loginBlock: () -> DanteUser?): Completable {
        return Single
            .fromCallable {
                loginBlock() ?: throw LoginException(errorMessage)
            }
            .doOnSuccess { user ->
                signInSubject.onNext(UserState.SignedInUser(user))
            }
            .fromSingleToCompletable()
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun logout(): Completable {
        return Completable
            .fromAction(fbAuth::signOut)
            .doOnComplete {
                signInSubject.onNext(UserState.Unauthenticated)
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun updateUserName(userName: String): Completable {
        return completableOf {
            val user = fbAuth.currentUser ?: throw NullPointerException("User is not logged in!")

            val update = UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build()

            Tasks.await(user.updateProfile(update))
        }
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