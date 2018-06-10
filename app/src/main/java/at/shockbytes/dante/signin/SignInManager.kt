package at.shockbytes.dante.signin

import android.content.Intent
import android.support.v4.app.FragmentActivity
import io.reactivex.Completable
import io.reactivex.Single

/**
 * @author Martin Macheiner
 * Date: 08-Jun-18.
 */

interface SignInManager {

    fun setup(activity: FragmentActivity)

    fun signIn(data: Intent): Single<DanteUser?>

    fun signOut(): Completable

    fun isSignedIn(activity: FragmentActivity): Single<Boolean>

    fun getAccount(activity: FragmentActivity): DanteUser?

}