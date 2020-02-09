package at.shockbytes.dante.ui.custom.rbc

data class RelativeBarChartData(
    val entries: List<RelativeBarChartEntry>
) {

    val absoluteValue: Float
        get() = entries.sumByDouble { it.value.toDouble() }.toFloat()

    fun startValueOf(position: Int): Float {
        return entries.subList(0, position).sumByDouble { it.value.toDouble() }.toFloat()
    }
}