package at.shockbytes.dante.core.data

import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.realm.RealmBookLabel

class RealmBookLabelMapper : Mapper<RealmBookLabel, BookLabel>() {

    override fun mapTo(data: RealmBookLabel): BookLabel {
        return BookLabel(
            title = data.title,
            hexColor = data.hexColor,
            bookId = data.bookId
        )
    }

    override fun mapFrom(data: BookLabel): RealmBookLabel {
        return RealmBookLabel(
            title = data.title,
            hexColor = data.hexColor,
            bookId = data.bookId
        )
    }
}