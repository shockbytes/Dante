package at.shockbytes.dante.core.login

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Author:  Martin Macheiner
 * Date:    08.06.2018
 */
@Parcelize
data class DanteUser(
    val givenName: String?,
    val displayName: String?,
    val email: String?,
    val photoUrl: Uri?,
    val authToken: String?,
    val userId: String,
    val authenticationSource: AuthenticationSource
) : Parcelable