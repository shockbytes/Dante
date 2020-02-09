package at.shockbytes.dante.ui.custom.rbc

data class RelativeBarChartData(
    private val entries: List<RelativeBarChartEntry>
) {

    val size: Int
        get() = filteredEntries.size

    val filteredEntries: List<RelativeBarChartEntry>
        get() = entries.filter { it.value > 0 }

    val absoluteValue: Float
        get() = entries.sumByDouble { it.value.toDouble() }.toFloat()

    fun startValueOf(position: Int): Float {
        return filteredEntries.subList(0, position).sumByDouble { it.value.toDouble() }.toFloat()
    }
}