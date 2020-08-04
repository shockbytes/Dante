package at.shockbytes.dante.util.sort

import androidx.annotation.StringRes
import at.shockbytes.dante.R

/**
 * Author:  Martin Macheiner
 * Date:    14.06.2018
 *
 * POSITION is default behavior and is always applied if the user drags items per hand.
 */
enum class SortStrategy(@StringRes val displayTitle: Int) {
    POSITION(R.string.sort_strategy_default),
    AUTHOR(R.string.sort_strategy_author),
    TITLE(R.string.sort_strategy_title),
    PROGRESS(R.string.sort_strategy_progress),
    PAGES(R.string.sort_strategy_pages),
    LABELS(R.string.sort_strategy_labels)
}