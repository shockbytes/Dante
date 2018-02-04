package at.shockbytes.dante.signin

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Martin Macheiner
 * Date: 30.12.2017.
 *
 * Use This class to signin with Firebase
 * https://firebase.google.com/docs/auth/android/google-signin
 *
 */

class GoogleSignInManager(private val prefs: SharedPreferences) {

    private var client: GoogleSignInClient? = null

    var maybeLater: Boolean
        get() = prefs.getBoolean(prefsMaybeLater, false)
        set(value) { prefs.edit().putBoolean(prefsMaybeLater, true).apply() }

    var showWelcomeScreen: Boolean
        get() = prefs.getBoolean(prefsShowWelcomeScreen, true)
        set(value) { prefs.edit().putBoolean(prefsShowWelcomeScreen, value).apply() }

    val signInIntent: Intent?
        get() = client?.signInIntent

    fun setup(activity: FragmentActivity) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
                .build()
        client = GoogleSignIn.getClient(activity, signInOptions)
    }

    fun signIn(data: Intent): Single<GoogleSignInAccount?> {
        return Single.fromCallable {
            Tasks.await(GoogleSignIn.getSignedInAccountFromIntent(data))
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    fun signOut(): Completable {
        return Completable.fromAction {
            Tasks.await(client?.signOut()!!)
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    fun isSignedIn(activity: FragmentActivity): Single<Boolean> {
        return Single.defer { Single.just(getAccount(activity) != null) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    fun getAccount(activity: FragmentActivity): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity)
    }

    fun loadAccountImage(activity: FragmentActivity, photoUrl: Uri): Single<Bitmap> {
        return Single.fromCallable {
            Picasso.with(activity).load(photoUrl).get()
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
    }

    companion object {

        const val rcSignIn = 0x8944
        private const val prefsMaybeLater = "prefs_google_login_maybe_later"
        private const val prefsShowWelcomeScreen = "prefs_google_show_welcome_screen"
    }


}