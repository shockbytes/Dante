package at.shockbytes.dante.signin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import at.shockbytes.dante.util.toDanteUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Author:  Martin Macheiner
 * Date:    30.12.2017.
 *
 * If migrating to firebase, use this docs
 * https://firebase.google.com/docs/auth/android/google-signin
 *
 */
class GoogleSignInManager(
    private val prefs: SharedPreferences,
    private val context: Context
) : SignInManager {

    private var client: GoogleSignInClient? = null

    private val signInSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()

    override var maybeLater: Boolean
        get() = prefs.getBoolean(prefsMaybeLater, false)
        set(value) {
            prefs.edit().putBoolean(prefsMaybeLater, value).apply()
        }

    override var showWelcomeScreen: Boolean
        get() = prefs.getBoolean(prefsShowWelcomeScreen, true)
        set(value) {
            prefs.edit().putBoolean(prefsShowWelcomeScreen, value).apply()
        }

    override val signInIntent: Intent?
        get() = client?.signInIntent

    override fun setup() {
        if (client == null) {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                    .build()
            client = GoogleSignIn.getClient(context, signInOptions)

            signInSubject.onNext(getAccount() != null)
        }
    }

    override fun signIn(data: Intent, signInToOnlineBackend: Boolean): Single<DanteUser?> {
        return Single.fromCallable {
            Tasks.await(GoogleSignIn.getSignedInAccountFromIntent(data))?.toDanteUser()
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { signInSubject.onNext(true) }
    }

    override fun signOut(): Completable {
        return Completable.fromAction {
            client?.let { Tasks.await(it.signOut()) }
            signInSubject.onNext(false)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun isSignedIn(): Observable<Boolean> {
        return signInSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    override fun getAccount(): DanteUser? {
        return GoogleSignIn.getLastSignedInAccount(context)?.toDanteUser()
    }

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    companion object {

        private const val prefsMaybeLater = "prefs_google_login_maybe_later"
        private const val prefsShowWelcomeScreen = "prefs_google_show_welcome_screen"
    }
}