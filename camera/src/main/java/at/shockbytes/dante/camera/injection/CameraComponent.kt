package at.shockbytes.dante.camera.injection

import at.shockbytes.dante.camera.BarcodeScanResultBottomSheetDialogFragment
import at.shockbytes.dante.core.injection.CoreComponent
import at.shockbytes.dante.core.injection.ModuleScope
import dagger.Component

@ModuleScope
@Component(dependencies = [CoreComponent::class])
interface CameraComponent {

    fun inject(fragment: BarcodeScanResultBottomSheetDialogFragment)
}