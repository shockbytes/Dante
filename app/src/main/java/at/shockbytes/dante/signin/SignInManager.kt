package at.shockbytes.dante.signin

import android.content.Context
import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 08-Jun-18.
 */

interface SignInManager {

    var maybeLater: Boolean

    var showWelcomeScreen: Boolean

    val signInIntent: Intent?

    fun setup(context: Context)

    fun signIn(data: Intent): Single<DanteUser?>

    fun signOut(): Completable

    fun isSignedIn(context: Context): Single<Boolean>

    fun getAccount(context: Context): DanteUser?

}