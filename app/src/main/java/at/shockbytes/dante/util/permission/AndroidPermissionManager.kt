package at.shockbytes.dante.util.permission

import androidx.fragment.app.FragmentActivity
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class AndroidPermissionManager : PermissionManager {

    override fun verifyPermissions(
        activity: FragmentActivity,
        permissions: Array<String>
    ): Boolean {
        return EasyPermissions.hasPermissions(activity, *permissions)
    }

    override fun requestPermissions(
        activity: FragmentActivity,
        permissions: Array<String>,
        requestCode: Int,
        rationale: Int,
        positiveButtonText: Int,
        negativeButtonText: Int
    ) {

        EasyPermissions.requestPermissions(
            PermissionRequest.Builder(activity, requestCode, *permissions)
                .setRationale(rationale)
                .setPositiveButtonText(positiveButtonText)
                .setNegativeButtonText(negativeButtonText)
                .build()
        )
    }
}