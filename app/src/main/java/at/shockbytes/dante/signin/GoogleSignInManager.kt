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
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017
 *
 * If migrating to firebase, use this docs
 * https://firebase.google.com/docs/auth/android/google-signin
 */
class GoogleSignInManager(
    prefs: SharedPreferences,
    private val context: Context,
    private val schedulers: SchedulerFacade
) : SignInManager {

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

            signInSubject.onNext(getAccount() != null)
        }
    }

    override fun signIn(data: Intent): Single<DanteUser> {
        return Single
            .fromCallable {
                Tasks
                    .await(GoogleSignIn.getSignedInAccountFromIntent(data))
                    .also(::firebaseAuthWithGoogle)
                    ?.toDanteUser()
                    ?: throw SignInException("Cannot sign into Google Account! DanteUser = null")

            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
            .doOnSubscribe { signInSubject.onNext(true) }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Timber.d("firebaseAuthWithGoogle:" + account.id)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        fbAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    val user = fbAuth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception,"signInWithCredential:failure")
                }

                // ...
            }
    }

    override fun signOut(): Completable {
        return Completable
            .fromAction {
                client?.let { Tasks.await(it.signOut()) }
                signInSubject.onNext(false)
            }
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun isSignedIn(): Observable<Boolean> {
        return signInSubject
            .observeOn(schedulers.ui)
            .subscribeOn(schedulers.io)
    }

    override fun getAccount(): DanteUser? {
        return GoogleSignIn.getLastSignedInAccount(context)?.toDanteUser()
    }

    override fun getAuthorizationHeader(): String {
        return SignInManager.getAuthorizationHeader(getGoogleAccount()?.idToken ?: "---")
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}