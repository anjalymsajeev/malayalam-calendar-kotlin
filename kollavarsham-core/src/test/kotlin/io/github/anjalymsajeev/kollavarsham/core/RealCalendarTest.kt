package io.github.anjalymsajeev.kollavarsham.core

import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Tests of the real (astronomical) calendar returned by [KollavarshamCalendar.create]. */
class RealCalendarTest {
    private val calendar = KollavarshamCalendar.create()

    private fun resource(name: String): List<String> =
        RealCalendarTest::class.java.getResourceAsStream("/reference/$name")!!.bufferedReader().readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }.drop(1)

    @Test fun `every observed month start in the reference dataset is reproduced`() {
        val rows = resource("month-starts.csv")
        assertTrue(rows.size >= 20, "reference dataset unexpectedly small: ${rows.size}")
        rows.forEach { row ->
            val (year, month, firstDay) = row.split(',')
            val first = LocalDate.parse(firstDay)
            val expected = MalayalamDate(year.toInt(), MalayalamMonth.valueOf(month), 1)
            assertEquals(expected, calendar.fromGregorian(first), row)
            assertEquals(first, calendar.toGregorian(expected), row)
            val dayBefore = calendar.fromGregorian(first.minusDays(1))
            assertEquals(expected.month.previous, dayBefore.month, row)
            assertEquals(calendar.monthLength(dayBefore.year, dayBefore.month), dayBefore.day, row)
        }
    }

    @Test fun `today's date and the corrected brief example`() {
        assertEquals(MalayalamDate(1202, MalayalamMonth.Thulam, 28), calendar.fromGregorian(LocalDate.of(2026, 11, 14)))
        assertEquals(LocalDate.of(2026, 11, 17), calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1)))
        assertEquals(30, calendar.monthLength(1202, MalayalamMonth.Thulam))
        assertEquals(MalayalamDate(1202, MalayalamMonth.Thulam, 28), LocalDate.of(2026, 11, 14).toMalayalamDate())
        assertEquals(LocalDate.of(2026, 11, 17), MalayalamDate(1202, MalayalamMonth.Vrischikam, 1).toLocalDate())
    }

    @Test fun `invariants hold for every day 1900-2100`() {
        var previous = calendar.fromGregorian(calendar.supportedRange.start)
        var day = calendar.supportedRange.start.plusDays(1)
        while (day <= calendar.supportedRange.endInclusive) {
            val current = calendar.fromGregorian(day)
            assertEquals(day, calendar.toGregorian(current), "round trip $day")
            if (current.month == previous.month) {
                assertEquals(previous.day + 1, current.day, "$day")
            } else {
                assertEquals(1, current.day, "$day")
                assertEquals(previous.month.next, current.month, "$day")
                assertTrue(previous.day in 29..32, "$day: month of ${previous.day} days")
                assertEquals(if (current.month == MalayalamMonth.Chingam) previous.year + 1 else previous.year, current.year, "$day")
            }
            // ME year = Gregorian year - 824 from Chingam 1, otherwise - 825.
            val offset = day.year - current.year
            assertTrue(offset == 824 || offset == 825, "$day -> $current")
            previous = current
            day = day.plusDays(1)
        }
    }

    @Test fun `every year has twelve months of 29-32 days totalling 365 or 366 days`() {
        for (year in 1076..1275) {
            val months = calendar.monthsOf(year)
            assertEquals(MalayalamMonth.entries, months.map { it.month })
            assertTrue(months.all { it.length in 29..32 }, "$year")
            assertTrue(months.sumOf { it.length } in 365..366, "$year: ${months.sumOf { it.length }}")
            months.forEach { b ->
                // The sankranti is on the first day or the day before it, and the margin explains which.
                val sankrantiDay = b.sankranti.atZone(KollavarshamCalendar.INDIAN_STANDARD_TIME).toLocalDate()
                if (b.source == BoundarySource.Computed) {
                    val expected = if (b.cutoffMargin > Duration.ZERO) sankrantiDay else sankrantiDay.plusDays(1)
                    assertEquals(expected, b.firstDay, "${b.month} $year")
                }
            }
        }
    }

    @Test fun `verified override for Thulam 1197 is applied and can be switched off`() {
        val thulam = calendar.monthBoundary(1197, MalayalamMonth.Thulam)
        assertEquals(BoundarySource.VerifiedOverride, thulam.source)
        assertEquals(LocalDate.of(2021, 10, 17), thulam.firstDay)
        assertTrue(thulam.cutoffMargin.abs() < Duration.ofMinutes(1))
        val raw = KollavarshamCalendar.create(CalendarConfiguration(applyVerifiedOverrides = false))
        assertEquals(BoundarySource.Computed, raw.monthBoundary(1197, MalayalamMonth.Thulam).source)
    }

    @Test fun `create returns a shared default and distinct custom calendars`() {
        assertSame(calendar, KollavarshamCalendar.create())
        assertSame(calendar, KollavarshamCalendar.create(CalendarConfiguration()))
        val kozhikode = KollavarshamCalendar.create(CalendarConfiguration(referenceLocation = GeoLocation(11.2588, 75.7804, "Kozhikode")))
        assertNotSame(calendar, kozhikode)
        // A different location moves the cut-off by minutes; ordinary dates are unaffected.
        assertEquals(calendar.fromGregorian(LocalDate.of(2026, 11, 14)), kozhikode.fromGregorian(LocalDate.of(2026, 11, 14)))
    }

    /**
     * Months whose sankranti is within 10 minutes of the cut-off, where almanacs may disagree.
     * Any change to this list (from an engine or rule change) must be reviewed: update
     * borderline-months.csv deliberately and explain why in the pull request.
     */
    @Test fun `borderline month list is unchanged`() {
        val raw = KollavarshamCalendar.create(CalendarConfiguration(applyVerifiedOverrides = false))
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val actual = (1075..1276).flatMap { raw.monthsOf(it) }
            .filter { it.cutoffMargin.abs() < Duration.ofMinutes(10) }
            .map { b ->
                listOf(
                    b.year, b.month, b.firstDay,
                    b.sankranti.atZone(KollavarshamCalendar.INDIAN_STANDARD_TIME).format(format),
                    "%.1f".format(java.util.Locale.ROOT, b.cutoffMargin.toMillis() / 60000.0),
                ).joinToString(",")
            }
        val expected = resource("borderline-months.csv")
        assertEquals(expected, actual, "Borderline months changed. Actual list:\n" + actual.joinToString("\n"))
    }
}
