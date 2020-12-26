package at.shockbytes.dante.core.login

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    08.06.2018
 */
interface LoginRepository {

    val signInIntent: Intent

    fun signInWithGoogle(data: Intent): Single<DanteUser>

    fun signInWithMail(mailAddress: String, password: String): Completable

    fun signInAnonymously(): Completable

    fun signOut(): Completable

    fun observeAccount(): Observable<UserState>

    fun getAccount(): Single<UserState>

    fun getAuthorizationHeader(): Single<String>

    /**
     * Default implementation for creating the bearer header
     */
    fun getAuthorizationHeader(authToken: String): String {
        return "Bearer $authToken"
    }
}