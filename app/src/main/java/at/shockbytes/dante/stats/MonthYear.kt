package at.shockbytes.dante.stats

import org.joda.time.DateTime

data class MonthYear(val month: Int, val year: Int) : Comparable<MonthYear> {

    val dateTime: DateTime
        get() = DateTime(year, month, 1, 0, 0, 0)

    override fun compareTo(other: MonthYear): Int {
        return if (year - other.year == 0) {
            month - other.month
        } else {
            year - other.year
        }
    }
}