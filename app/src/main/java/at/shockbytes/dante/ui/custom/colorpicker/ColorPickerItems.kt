package at.shockbytes.dante.ui.custom.colorpicker

object ColorPickerItems {

    fun fromColorResources(resources: List<Int>, preSelectedIndex: Int?): List<ColorPickerItem> {
        return resources.mapIndexed { index, colorResource ->
            ColorPickerItem(colorResource, isSelected = index == preSelectedIndex)
        }
    }
}