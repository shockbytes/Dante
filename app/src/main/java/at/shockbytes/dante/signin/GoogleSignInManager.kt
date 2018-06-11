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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author  Martin Macheiner
 * Date:    30.12.2017.
 *
 * Use This class to signin with Firebase
 * https://firebase.google.com/docs/auth/android/google-signin
 *
 */

class GoogleSignInManager(private val prefs: SharedPreferences): SignInManager {

    private var client: GoogleSignInClient? = null

    override var maybeLater: Boolean
        get() = prefs.getBoolean(prefsMaybeLater, false)
        set(value) { prefs.edit().putBoolean(prefsMaybeLater, true).apply() }

    override var showWelcomeScreen: Boolean
        get() = prefs.getBoolean(prefsShowWelcomeScreen, true)
        set(value) { prefs.edit().putBoolean(prefsShowWelcomeScreen, value).apply() }

    override val signInIntent: Intent?
        get() = client?.signInIntent

    override fun setup(context: Context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .build()
        client = GoogleSignIn.getClient(context, signInOptions)
    }

    override fun signIn(data: Intent): Single<DanteUser?> {
        return Single.fromCallable {
            Tasks.await(GoogleSignIn.getSignedInAccountFromIntent(data))?.toDanteUser()
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun signOut(): Completable {
        return Completable.fromAction {
            Tasks.await(client?.signOut()!!)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    override fun isSignedIn(context: Context): Single<Boolean> {
        return Single.defer { Single.just(getAccount(context) != null) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    override fun getAccount(context: Context): DanteUser? {
        return GoogleSignIn.getLastSignedInAccount(context)?.toDanteUser()
    }

    fun getGoogleAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    companion object {

        const val rcSignIn = 0x8944
        private const val prefsMaybeLater = "prefs_google_login_maybe_later"
        private const val prefsShowWelcomeScreen = "prefs_google_show_welcome_screen"
    }


}