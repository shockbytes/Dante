package at.shockbytes.dante.core.book

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.core.R

enum class Languages(@StringRes val title: Int, @DrawableRes val image: Int) {

    OTHER(R.string.language_other, R.drawable.ic_language_other),
    ENGLISH(R.string.language_english_full, R.drawable.ic_language_english),
    GERMAN(R.string.language_german_full, R.drawable.ic_language_german),
    ITALIAN(R.string.language_italian_full, R.drawable.ic_language_italian),
    FRENCH(R.string.language_french_full, R.drawable.ic_language_french),
    SPANISH(R.string.language_spanish_full, R.drawable.ic_language_spanish),
    PORTUGUESE(R.string.language_portuguese_full, R.drawable.ic_language_portuguese),
    DUTCH(R.string.language_dutch_full, R.drawable.ic_language_dutch),
    CHINESE(R.string.language_chinese_full, R.drawable.ic_language_chinese),
    RUSSIAN(R.string.language_russian_full, R.drawable.ic_language_russian),
    SWEDISH(R.string.language_swedish_full, R.drawable.ic_language_swedish),
    NORWEGIAN(R.string.language_norwegian_full, R.drawable.ic_language_norwegian),
    POLISH(R.string.language_polish_full, R.drawable.ic_language_polish),
    RUMANIAN(R.string.language_romanian_full, R.drawable.ic_language_romanian),
    CROATIAN(R.string.language_croatian_full, R.drawable.ic_language_croatian),
    HUNGARIAN(R.string.language_hungary_full, R.drawable.ic_language_hungary),
    INDONESIAN(R.string.language_indonesian_full, R.drawable.ic_language_indonesian),
    THAI(R.string.language_thai_full, R.drawable.ic_language_thai)
}