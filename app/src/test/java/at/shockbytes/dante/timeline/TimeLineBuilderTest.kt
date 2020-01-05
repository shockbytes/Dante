package at.shockbytes.dante.timeline

import at.shockbytes.dante.core.book.BookEntity
import at.shockbytes.dante.core.book.BookState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeLineBuilderTest {

    @Test
    fun `test buildTimeLineItems, empty list`() {

        val input = listOf<BookEntity>()

        val actual = TimeLineBuilder.buildTimeLineItems(input)
        val expected = listOf<TimeLineItem>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `test buildTimeLineItems, no book is in read state`() {

        val input = listOf(
            BookEntity(state = BookState.READ_LATER),
            BookEntity(state = BookState.READ_LATER)
        )

        val actual = TimeLineBuilder.buildTimeLineItems(input)
        val expected = listOf<TimeLineItem>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `test buildTimeLineItems, books finished, but have no started date`() {

        val input = listOf(
            BookEntity(state = BookState.READ, startDate = 0L),
            BookEntity(state = BookState.READ, startDate = 0L)
        )

        val actual = TimeLineBuilder.buildTimeLineItems(input)
        val expected = listOf<TimeLineItem>()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `test buildTimeLineItems, single month`() {

        // 5th January 2020
        val startDate = 1578212957000L

        val input = listOf(
            BookEntity(state = BookState.READ, startDate = startDate, title = "Daily Stoic", thumbnailAddress = "ds.png"),
            BookEntity(state = BookState.READ, startDate = startDate - 1, title = "Test", thumbnailAddress = null),
            BookEntity(state = BookState.READING, startDate = startDate - 2, title = "Moby Dick", thumbnailAddress = "moby-dick.png")
        )

        val actual = TimeLineBuilder.buildTimeLineItems(input)
        val expected = listOf(
            TimeLineItem.MonthHeader(1, 2020),
            TimeLineItem.BookTimeLineItem("Daily Stoic", "ds.png"),
            TimeLineItem.BookTimeLineItem("Test", null),
            TimeLineItem.BookTimeLineItem("Moby Dick", "moby-dick.png"),
            TimeLineItem.DanteInstall
        )

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `test buildTimeLineItems, two months with invalid states in between`() {

        // 5th January 2020
        val startDate = 1578212957000L
        // 5th February 2020
        val startDate2 = 1580891357000L

        val input = listOf(
            BookEntity(state = BookState.READ, startDate = startDate, title = "Daily Stoic", thumbnailAddress = "ds.png"),
            BookEntity(state = BookState.READ, startDate = startDate - 1, title = "Test", thumbnailAddress = null),
            BookEntity(state = BookState.READING, startDate = startDate - 2, title = "Moby Dick", thumbnailAddress = "moby-dick.png"),
            BookEntity(state = BookState.READ_LATER),
            BookEntity(state = BookState.READ, startDate = 0L, title = "Test", thumbnailAddress = null),
            BookEntity(state = BookState.READ, startDate = startDate2, title = "Tom Sawyer", thumbnailAddress = "ts.png")
        )

        val actual = TimeLineBuilder.buildTimeLineItems(input)
        val expected = listOf(
            TimeLineItem.MonthHeader(2, 2020),
            TimeLineItem.BookTimeLineItem("Tom Sawyer", "ts.png"),
            TimeLineItem.MonthHeader(1, 2020),
            TimeLineItem.BookTimeLineItem("Daily Stoic", "ds.png"),
            TimeLineItem.BookTimeLineItem("Test", null),
            TimeLineItem.BookTimeLineItem("Moby Dick", "moby-dick.png"),
            TimeLineItem.DanteInstall
        )

        assertThat(actual).isEqualTo(expected)
    }
}