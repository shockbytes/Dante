package at.shockbytes.dante.stats

sealed class BookStatsItem {

    object BooksAndPages : BookStatsItem()

    object ReadingDuration : BookStatsItem()

    object Favorites : BookStatsItem()

    object Languages : BookStatsItem()

    object Others : BookStatsItem()
}