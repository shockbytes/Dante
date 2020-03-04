package at.shockbytes.dante.signin

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    08.06.2018
 */
interface SignInManager {

    companion object {

        fun getAuthorizationHeader(authToken: String): String {
            return "Bearer $authToken"
        }
    }

    var maybeLater: Boolean

    var showWelcomeScreen: Boolean

    val signInIntent: Intent?

    fun setup()

    fun signIn(data: Intent): Single<DanteUser>

    fun signOut(): Completable

    fun observeSignInState(): Observable<UserState>

    fun getAccount(): Single<UserState>

    fun getAuthorizationHeader(): Single<String>

    fun close()
}