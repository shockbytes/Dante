package at.shockbytes.dante.books

import at.shockbytes.dante.util.books.Book

/**
 * @author Martin Macheiner
 * Date: 13.01.2018.
 */

object BookFactory {

    fun resolve(title: String, subtitle: String, author: String, pageCount: Int,
                publishedDate: String, isbn: String, thumbnailAddress: String?,
                googleBooksLink: String?, language: String): Book {

        return Book(title = title, subTitle = subtitle, author = author, pageCount = pageCount,
                publishedDate = publishedDate, isbn =  isbn, thumbnailAddress = thumbnailAddress,
                googleBooksLink = googleBooksLink, language = language)
    }
}