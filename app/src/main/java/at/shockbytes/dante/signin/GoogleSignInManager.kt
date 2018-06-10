package at.shockbytes.dante.signin

import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.FragmentActivity
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

    var maybeLater: Boolean
        get() = prefs.getBoolean(prefsMaybeLater, false)
        set(value) { prefs.edit().putBoolean(prefsMaybeLater, true).apply() }

    var showWelcomeScreen: Boolean
        get() = prefs.getBoolean(prefsShowWelcomeScreen, true)
        set(value) { prefs.edit().putBoolean(prefsShowWelcomeScreen, value).apply() }

    val signInIntent: Intent?
        get() = client?.signInIntent

    override fun setup(activity: FragmentActivity) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .build()
        client = GoogleSignIn.getClient(activity, signInOptions)
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

    override fun isSignedIn(activity: FragmentActivity): Single<Boolean> {
        return Single.defer { Single.just(getAccount(activity) != null) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    override fun getAccount(activity: FragmentActivity): DanteUser? {
        return GoogleSignIn.getLastSignedInAccount(activity)?.toDanteUser()
    }

    fun getGoogleAccount(activity: FragmentActivity): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity)
    }

    companion object {

        const val rcSignIn = 0x8944
        private const val prefsMaybeLater = "prefs_google_login_maybe_later"
        private const val prefsShowWelcomeScreen = "prefs_google_show_welcome_screen"
    }


}