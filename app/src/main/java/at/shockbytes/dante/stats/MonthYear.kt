package at.shockbytes.dante.stats

data class MonthYear(val month: Int, val year: Int): Comparable<MonthYear> {

    override fun compareTo(other: MonthYear): Int {
        return if (year - other.year == 0) {
            month - other.month
        } else {
            year - other.year
        }
    }
}