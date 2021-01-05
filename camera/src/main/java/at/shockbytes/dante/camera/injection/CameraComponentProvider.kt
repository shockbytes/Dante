package at.shockbytes.dante.camera.injection

import android.content.Context
import at.shockbytes.dante.core.injection.CoreInjectHelper

object CameraComponentProvider {

    private var cameraComponent: CameraComponent? = null

    fun get(context: Context): CameraComponent {

        if (cameraComponent == null) {
            cameraComponent = DaggerCameraComponent
                .builder()
                .coreComponent(CoreInjectHelper.provideCoreComponent(context))
                .build()
        }

        return cameraComponent!!
    }
}