package at.shockbytes.dante.stats

data class BooksPagesInfo(
    val books: Books,
    val pages: Pages
) {

    data class Books(
        val waiting: Int,
        val reading: Int,
        val read: Int
    )

    data class Pages(
        val read: Int,
        val waiting: Int
    )
}