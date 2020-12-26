package at.shockbytes.dante.signin

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    08.06.2018
 */
interface SignInRepository {

    val signInIntent: Intent?

    fun setup()

    fun signIn(data: Intent): Single<DanteUser>

    fun signOut(): Completable

    fun observeSignInState(): Observable<UserState>

    fun getAccount(): Single<UserState>

    fun getAuthorizationHeader(): Single<String>

    /**
     * Default implementation for creating the bearer header
     */
    fun getAuthorizationHeader(authToken: String): String {
        return "Bearer $authToken"
    }
}