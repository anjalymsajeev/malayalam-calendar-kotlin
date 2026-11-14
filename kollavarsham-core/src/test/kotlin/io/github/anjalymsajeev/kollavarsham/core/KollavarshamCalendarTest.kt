package io.github.anjalymsajeev.kollavarsham.core

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConversionTest {
    private val calendar = SyntheticBoundarySource.calendar()

    @Test fun `every supported day round-trips and advances by exactly one`() {
        val range = calendar.supportedRange
        var previous = calendar.fromGregorian(range.start)
        assertEquals(range.start, calendar.toGregorian(previous))
        var day = range.start.plusDays(1)
        while (day <= range.endInclusive) {
            val current = calendar.fromGregorian(day)
            assertEquals(day, calendar.toGregorian(current), "round trip $day")
            if (current.month == previous.month) {
                assertEquals(previous.day + 1, current.day, "$day")
                assertEquals(previous.year, current.year, "$day")
            } else {
                assertEquals(1, current.day, "$day")
                assertEquals(previous.month.next, current.month, "$day")
                assertEquals(calendar.monthLength(previous.year, previous.month), previous.day, "$day")
                val expectedYear = if (current.month == MalayalamMonth.Chingam) previous.year + 1 else previous.year
                assertEquals(expectedYear, current.year, "$day")
            }
            previous = current
            day = day.plusDays(1)
        }
    }

    @Test fun `ME year equals Gregorian year minus 824 from Chingam 1, else minus 825`() {
        var day = LocalDate.of(1990, 1, 1)
        while (day.year <= 2030) {
            val date = calendar.fromGregorian(day)
            val chingam = calendar.monthBoundary(day.year - 824, MalayalamMonth.Chingam).firstDay
            assertEquals(if (day >= chingam) day.year - 824 else day.year - 825, date.year, "$day")
            day = day.plusDays(1)
        }
    }

    @Test fun `month boundaries match the source exactly`() {
        val year = 1202
        val months = calendar.monthsOf(year)
        assertEquals(MalayalamMonth.entries, months.map { it.month })
        months.forEach { b ->
            assertEquals(SyntheticBoundarySource.lengthOf(MonthKey(year, b.month)), b.length)
            assertEquals(MalayalamDate(year, b.month, 1), calendar.fromGregorian(b.firstDay))
            assertEquals(MalayalamDate(year, b.month, b.length), calendar.fromGregorian(b.lastDay))
            assertEquals(BoundarySource.Computed, b.source)
        }
        months.zipWithNext().forEach { (a, b) -> assertEquals(a.lastDay.plusDays(1), b.firstDay) }
    }

    @Test fun `Gregorian leap days convert and round-trip`() {
        listOf(LocalDate.of(2000, 2, 29), LocalDate.of(2024, 2, 29), LocalDate.of(1904, 2, 29), LocalDate.of(2096, 2, 29))
            .forEach { assertEquals(it, calendar.toGregorian(calendar.fromGregorian(it))) }
    }

    @Test fun `dates outside the supported range are rejected explicitly`() {
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.fromGregorian(LocalDate.of(1899, 12, 31)) }
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.fromGregorian(LocalDate.of(2101, 1, 1)) }
        // Chingam 1, 1075 falls in August 1899, before the supported range.
        val early = MalayalamDate(1075, MalayalamMonth.Chingam, 1)
        assertIs<DateValidation.OutOfSupportedRange>(calendar.validate(early))
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.toGregorian(early) }
        assertNull(calendar.toGregorianOrNull(early))
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.monthBoundary(1300, MalayalamMonth.Chingam) }
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.monthsOf(1074) }
    }

    @Test fun `days beyond the month length are invalid, never rolled over`() {
        val length = calendar.monthLength(1202, MalayalamMonth.Thulam)
        val valid = MalayalamDate(1202, MalayalamMonth.Thulam, length)
        val invalid = MalayalamDate(1202, MalayalamMonth.Thulam, length + 1)
        assertTrue(calendar.isValid(valid))
        assertFalse(calendar.isValid(invalid))
        assertEquals(DateValidation.DayOutOfRange(length + 1, length), calendar.validate(invalid))
        val error = assertFailsWith<InvalidMalayalamDateException> { calendar.toGregorian(invalid) }
        assertEquals(invalid, error.date)
        assertTrue(error.message!!.contains("$length days"))
        assertNull(calendar.toGregorianOrNull(invalid))
    }

    @Test fun `today uses the supplied clock`() {
        val clock = Clock.fixed(Instant.parse("2026-11-14T06:00:00Z"), ZoneOffset.UTC)
        assertEquals(calendar.fromGregorian(LocalDate.of(2026, 11, 14)), calendar.today(clock))
    }
}

class ArithmeticTest {
    private val calendar = SyntheticBoundarySource.calendar()
    private val start = MalayalamDate(1202, MalayalamMonth.Chingam, 1)

    @Test fun `plusDays is invertible and matches Gregorian arithmetic`() {
        for (n in listOf(0L, 1L, 29L, 30L, 31L, 365L, 366L, 1000L, -1L, -400L)) {
            val moved = calendar.plusDays(start, n)
            assertEquals(start, calendar.plusDays(moved, -n), "n=$n")
            assertEquals(n, calendar.daysBetween(start, moved), "n=$n")
            assertEquals(calendar.toGregorian(start).plusDays(n), calendar.toGregorian(moved))
        }
    }

    @Test fun `plusDays crosses month and year boundaries`() {
        val lastOfYear = MalayalamDate(1201, MalayalamMonth.Karkidakam, calendar.monthLength(1201, MalayalamMonth.Karkidakam))
        assertEquals(start, calendar.plusDays(lastOfYear, 1))
        assertEquals(lastOfYear, calendar.plusDays(start, -1))
    }

    @Test fun `plusMonths keeps the day and clamps to the target month length`() {
        assertEquals(MalayalamDate(1203, MalayalamMonth.Chingam, 1), calendar.plusMonths(start, 12))
        assertEquals(MalayalamDate(1201, MalayalamMonth.Medam, 1), calendar.plusMonths(start, -4))
        // Synthetic Edavam has 31 days; Mithunam has 32; Karkidakam has 31.
        val day32 = MalayalamDate(1202, MalayalamMonth.Mithunam, 32)
        assertEquals(MalayalamDate(1202, MalayalamMonth.Karkidakam, 31), calendar.plusMonths(day32, 1))
        assertEquals(MalayalamDate(1202, MalayalamMonth.Makaram, 29), calendar.plusMonths(day32, -5))
    }

    @Test fun `arithmetic rejects invalid inputs and out-of-range results`() {
        assertFailsWith<InvalidMalayalamDateException> { calendar.plusDays(MalayalamDate(1202, MalayalamMonth.Dhanu, 32), 1) }
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.plusDays(start, 100_000) }
        assertFailsWith<DateOutOfSupportedRangeException> { calendar.plusMonths(start, 12 * 200) }
    }

    @Test fun `daysBetween is antisymmetric`() {
        val end = MalayalamDate(1205, MalayalamMonth.Medam, 14)
        assertEquals(-calendar.daysBetween(start, end), calendar.daysBetween(end, start))
    }
}
