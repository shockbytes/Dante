package at.shockbytes.dante.signin

import android.content.Context
import android.content.Intent
import at.shockbytes.dante.R
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.toDanteUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
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
class GoogleFirebaseSignInRepository(
    private val context: Context,
    private val schedulers: SchedulerFacade
) : SignInRepository {

    private var client: GoogleSignInClient? = null

    private val fbAuth = FirebaseAuth.getInstance()

    private val signInSubject: BehaviorSubject<UserState> = BehaviorSubject.create()

    override val signInIntent: Intent?
        get() = client?.signInIntent

    override fun setup() {
        if (client == null) {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.oauth_client_id))
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .build()
            client = GoogleSignIn.getClient(context, signInOptions)
        }

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

    override fun signIn(data: Intent): Single<DanteUser> {
        return Single
            .fromCallable {
                Tasks
                    .await(GoogleSignIn.getSignedInAccountFromIntent(data))
                    .authenticateToFirebase()
                    ?: throw SignInException("Cannot sign into Google Account! DanteUser = null")
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
            .doOnSuccess { user ->
                signInSubject.onNext(UserState.SignedInUser(user))
            }
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

    override fun observeSignInState(): Observable<UserState> {
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
}