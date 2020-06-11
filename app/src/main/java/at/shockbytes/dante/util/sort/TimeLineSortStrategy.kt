package at.shockbytes.dante.util.sort

/**
 * Author:  Martin Macheiner
 * Date:    11.06.2020
 */
enum class TimeLineSortStrategy {
    SORT_BY_START_DATE,
    SORT_BY_END_DATE;

    companion object {
        fun ofOrdinal(ordinal: Int): TimeLineSortStrategy {
            return values()[ordinal]
        }
    }
}