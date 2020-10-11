package at.shockbytes.dante.ui.adapter.stats.model

import androidx.annotation.StringRes
import at.shockbytes.dante.R

enum class ReadingGoalType(
    @StringRes val title: Int,
    @StringRes val labelTemplate: Int,
    val sliderValueTo: Float,
    val sliderValueFrom: Float,
    val sliderStepSize: Float
) {
    PAGES(
        R.string.reading_goal_pages,
        R.string.pages_formatted,
        sliderValueFrom = 30f,
        sliderValueTo = 3000f,
        sliderStepSize = 10f
    ),
    BOOKS(
        R.string.reading_goal_books,
        R.string.books_formatted,
        sliderValueFrom = 1f,
        sliderValueTo = 30f,
        sliderStepSize = 1f
    );
}