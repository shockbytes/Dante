package at.shockbytes.dante.core.injection

import android.content.Context
import at.shockbytes.dante.core.R
import at.shockbytes.dante.core.login.GoogleFirebaseLoginRepository
import at.shockbytes.dante.core.login.LoginRepository
import at.shockbytes.dante.util.scheduler.SchedulerFacade
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LoginModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.oauth_client_id))
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.DRIVE_FILE))
            .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    @Provides
    @Singleton
    fun provideLoginRepository(
        signInClient: GoogleSignInClient,
        schedulers: SchedulerFacade
    ): LoginRepository {
        return GoogleFirebaseLoginRepository(signInClient, context, schedulers)
    }
}