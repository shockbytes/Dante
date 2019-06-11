package at.shockbytes.dante.util.permission

import androidx.fragment.app.FragmentActivity

/**
 * Test implementation of [PermissionManager] which always grants the permissions.
 */
class TestPermissionManager : PermissionManager {

    override fun verifyPermissions(
        activity: FragmentActivity,
        permissions: Array<String>
    ): Boolean = true

    override fun requestPermissions(
        activity: FragmentActivity,
        permissions: Array<String>,
        requestCode: Int,
        rationale: Int,
        positiveButtonText: Int,
        negativeButtonText: Int
    ) = Unit // Do nothing
}