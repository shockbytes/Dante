package at.shockbytes.dante.core.book

enum class BookState {
    READ_LATER,
    READING,
    READ,
    WISHLIST;

    companion object {
        fun fromString(name: String?, defaultValue: BookState = READ_LATER): BookState {
            return values().find { it.name == name } ?: defaultValue
        }
    }
}
