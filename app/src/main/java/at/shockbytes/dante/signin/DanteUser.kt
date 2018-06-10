package at.shockbytes.dante.signin

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * @author  Martin Macheiner
 * Date:    08-Jun-18.
 */
@Parcelize
data class DanteUser(val givenName: String?,
                     val displayName: String?,
                     val email: String?,
                     val photoUrl: Uri?,
                     val providerId: String?,
                     val authToken: String?) : Parcelable