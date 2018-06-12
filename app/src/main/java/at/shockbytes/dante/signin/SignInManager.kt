package at.shockbytes.dante.signin

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 08-Jun-18.
 */

interface SignInManager {

    var maybeLater: Boolean

    var showWelcomeScreen: Boolean

    val signInIntent: Intent?

    fun setup()

    fun signIn(data: Intent): Single<DanteUser?>

    fun signOut(): Completable

    fun isSignedIn(): Observable<Boolean>

    fun getAccount(): DanteUser?

}