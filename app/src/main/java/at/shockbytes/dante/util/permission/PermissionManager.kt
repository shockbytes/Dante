package at.shockbytes.dante.util.permission

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity

interface PermissionManager {

    fun verifyPermissions(
        activity: FragmentActivity,
        permissions: Array<String>
    ): Boolean

    fun requestPermissions(
        activity: FragmentActivity,
        permissions: Array<String>,
        requestCode: Int,
        @StringRes rationale: Int,
        @StringRes positiveButtonText: Int,
        @StringRes negativeButtonText: Int
    )
}