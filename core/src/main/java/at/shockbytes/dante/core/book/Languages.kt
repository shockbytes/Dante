package at.shockbytes.dante.core.book

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.shockbytes.dante.core.R

enum class Languages(
    @StringRes val title: Int,
    @DrawableRes val image: Int,
    val code: String
) {

    OTHER(R.string.language_other, R.drawable.ic_language_other, "na"),
    ENGLISH(R.string.language_english_full, R.drawable.ic_language_english, "en"),
    GERMAN(R.string.language_german_full, R.drawable.ic_language_german, "de"),
    ITALIAN(R.string.language_italian_full, R.drawable.ic_language_italian, "it"),
    FRENCH(R.string.language_french_full, R.drawable.ic_language_french, "fr"),
    SPANISH(R.string.language_spanish_full, R.drawable.ic_language_spanish, "es"),
    PORTUGUESE(R.string.language_portuguese_full, R.drawable.ic_language_portuguese, "pt"),
    DUTCH(R.string.language_dutch_full, R.drawable.ic_language_dutch, "nl"),
    CHINESE(R.string.language_chinese_full, R.drawable.ic_language_chinese, "cn"),
    RUSSIAN(R.string.language_russian_full, R.drawable.ic_language_russian, "ru"),
    SWEDISH(R.string.language_swedish_full, R.drawable.ic_language_swedish, "se"),
    NORWEGIAN(R.string.language_norwegian_full, R.drawable.ic_language_norwegian, "no"),
    POLISH(R.string.language_polish_full, R.drawable.ic_language_polish, "pl"),
    RUMANIAN(R.string.language_romanian_full, R.drawable.ic_language_romanian, "ro"),
    CROATIAN(R.string.language_croatian_full, R.drawable.ic_language_croatian, "hr"),
    HUNGARIAN(R.string.language_hungary_full, R.drawable.ic_language_hungary, "hu"),
    INDONESIAN(R.string.language_indonesian_full, R.drawable.ic_language_indonesian, "id"),
    THAI(R.string.language_thai_full, R.drawable.ic_language_thai, "th");

    companion object {

        fun fromLanguageCode(code: String): Languages {
            return values().find { it.code == code } ?: OTHER
        }
    }
}