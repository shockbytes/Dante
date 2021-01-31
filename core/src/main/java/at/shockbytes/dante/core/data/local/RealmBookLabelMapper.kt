package at.shockbytes.dante.core.data.local

import at.shockbytes.dante.core.book.BookLabel
import at.shockbytes.dante.core.book.realm.RealmBookLabel
import at.shockbytes.dante.core.data.Mapper
import at.shockbytes.dante.util.HexColor

class RealmBookLabelMapper : Mapper<RealmBookLabel, BookLabel>() {

    override fun mapTo(data: RealmBookLabel): BookLabel {
        return BookLabel(
            title = data.title,
            hexColor = HexColor.ofString(data.hexColor),
            bookId = data.bookId
        )
    }

    override fun mapFrom(data: BookLabel): RealmBookLabel {
        return RealmBookLabel(
            title = data.title,
            hexColor = data.hexColor.asString(),
            bookId = data.bookId
        )
    }
}