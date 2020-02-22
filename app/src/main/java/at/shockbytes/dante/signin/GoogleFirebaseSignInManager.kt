package at.shockbytes.dante.signin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import at.shockbytes.dante.R
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import at.shockbytes.dante.util.settings.delegate.SharedPreferencesBoolPropertyDelegate
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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 *
 * If migrating to firebase, use this docs
 * https://firebase.google.com/docs/auth/android/google-signin
 */
class GoogleFirebaseSignInManager(
    prefs: SharedPreferences,
    private val context: Context,
    private val schedulers: SchedulerFacade
) : SignInManager {

    private val compositeDisposable = CompositeDisposable()

    private var client: GoogleSignInClient? = null

    private val fbAuth = FirebaseAuth.getInstance()

    private val signInSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()

    override var maybeLater: Boolean by SharedPreferencesBoolPropertyDelegate(prefs, "prefs_google_login_maybe_later", defaultValue = true)

    override var showWelcomeScreen: Boolean by SharedPreferencesBoolPropertyDelegate(prefs, "prefs_show_welcome_screen", defaultValue = true)

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

            getAccount()
                .subscribeOn(schedulers.io)
                .subscribe({
                    signInSubject.onNext(true)
                }, { throwable ->
                    Timber.e(throwable)
                    signInSubject.onNext(false)
                })
                .addTo(compositeDisposable)
        }
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
            .doOnSuccess {
                signInSubject.onNext(true)
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
                signInSubject.onNext(false)
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun observeSignInState(): Observable<Boolean> {
        return signInSubject
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun getAccount(): Single<DanteUser> {
        return Single
            .fromCallable {
                fbAuth.currentUser?.toDanteUser() ?: throw IllegalStateException("No account signed in")
            }
            .subscribeOn(schedulers.io)
    }

    override fun getAuthorizationHeader(): Single<String> {
        return getAccount().map { account ->
            SignInManager.getAuthorizationHeader(account.authToken ?: "")
        }
    }

    override fun close() {
        compositeDisposable.clear()
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}