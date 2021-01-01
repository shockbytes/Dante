package at.shockbytes.dante.core.login

import android.content.Intent
import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Author:  Martin Macheiner
 * Date:    08.06.2018
 */
interface LoginRepository {

    fun loginWithGoogle(data: Intent): Completable

    fun fetchSignInMethodsForEmail(mailAddress: String): Single<List<String>>

    fun createAccountWithMail(mailAddress: String, password: String): Completable

    fun loginWithMail(mailAddress: String, password: String): Completable

    fun loginAnonymously(): Completable

    fun logout(): Completable

    fun upgradeAnonymousAccount(mailAddress: String, password: String): Completable

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