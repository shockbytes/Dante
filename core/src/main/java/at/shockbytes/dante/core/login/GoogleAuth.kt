package at.shockbytes.dante.core.login

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class GoogleAuth(
    client: GoogleSignInClient,
    private val context: Context
) {

    val googleLoginIntent: Intent = client.signInIntent

    fun getGoogleAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}