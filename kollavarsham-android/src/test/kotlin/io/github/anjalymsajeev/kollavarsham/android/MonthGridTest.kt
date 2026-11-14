package io.github.anjalymsajeev.kollavarsham.android

import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonthGridTest {
    @Test
    fun gregorianMonthCoversWholeWeeksWithBothDates() {
        val grid = MonthGrid.ofGregorianMonth(YearMonth.of(2026, 11))
        assertTrue(grid.weeks.all { it.size == 7 })
        assertEquals(
            DayOfWeek.SUNDAY,
            grid.weeks
                .first()
                .first()
                .date.dayOfWeek,
        )
        assertEquals(
            DayOfWeek.SATURDAY,
            grid.weeks
                .last()
                .last()
                .date.dayOfWeek,
        )
        assertEquals(30, grid.cells.count { it.inMonth })
        // 1 November 2026 is a Sunday, so no leading padding.
        assertEquals(LocalDate.of(2026, 11, 1), grid.cells.first().date)
        val nov14 = grid.cells.single { it.date == LocalDate.of(2026, 11, 14) }
        assertEquals(MalayalamDate(1202, MalayalamMonth.Thulam, 28), nov14.malayalamDate)
        assertEquals(listOf(1202 to MalayalamMonth.Thulam, 1202 to MalayalamMonth.Vrischikam), grid.malayalamMonths)
    }

    @Test
    fun malayalamMonthAndCustomFirstDayOfWeek() {
        val grid = MonthGrid.ofMalayalamMonth(1202, MalayalamMonth.Vrischikam, firstDayOfWeek = DayOfWeek.MONDAY)
        val inMonth = grid.cells.filter { it.inMonth }
        assertEquals(LocalDate.of(2026, 11, 17), inMonth.first().date)
        assertEquals(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1), inMonth.first().malayalamDate)
        assertEquals(29, inMonth.size)
        assertTrue(grid.weeks.all { it.first().date.dayOfWeek == DayOfWeek.MONDAY })
        // Cells are consecutive days.
        grid.cells.zipWithNext().forEach { (a, b) -> assertEquals(a.date.plusDays(1), b.date) }
    }
}
