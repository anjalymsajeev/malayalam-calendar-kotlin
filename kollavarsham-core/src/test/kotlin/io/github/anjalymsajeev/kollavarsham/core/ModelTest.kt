package io.github.anjalymsajeev.kollavarsham.core

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MalayalamMonthTest {
    @Test fun `months are in Chingam-first order with numbers 1 to 12`() {
        assertEquals(
            listOf(
                "Chingam",
                "Kanni",
                "Thulam",
                "Vrischikam",
                "Dhanu",
                "Makaram",
                "Kumbham",
                "Meenam",
                "Medam",
                "Edavam",
                "Mithunam",
                "Karkidakam",
            ),
            MalayalamMonth.entries.map { it.name },
        )
        MalayalamMonth.entries.forEachIndexed { i, m -> assertEquals(i + 1, m.number) }
    }

    @Test fun `next and previous wrap around the year`() {
        assertEquals(MalayalamMonth.Chingam, MalayalamMonth.Karkidakam.next)
        assertEquals(MalayalamMonth.Karkidakam, MalayalamMonth.Chingam.previous)
        MalayalamMonth.entries.forEach {
            assertEquals(it, it.next.previous)
            assertEquals(it, it.previous.next)
        }
    }

    @Test fun `of resolves numbers and rejects others`() {
        MalayalamMonth.entries.forEach { assertEquals(it, MalayalamMonth.of(it.number)) }
        assertFailsWith<IllegalArgumentException> { MalayalamMonth.of(0) }
        assertFailsWith<IllegalArgumentException> { MalayalamMonth.of(13) }
    }
}

class MalayalamWeekdayTest {
    @Test fun `maps one to one to DayOfWeek`() {
        DayOfWeek.entries.forEach { assertEquals(it, MalayalamWeekday.of(it).dayOfWeek) }
        assertEquals(
            7,
            MalayalamWeekday.entries
                .map { it.dayOfWeek }
                .toSet()
                .size,
        )
        assertEquals(MalayalamWeekday.Njayar, MalayalamWeekday.of(DayOfWeek.SUNDAY))
    }

    @Test fun `LocalDate extension gives the weekday`() {
        assertEquals(MalayalamWeekday.Shani, LocalDate.of(2026, 11, 14).malayalamWeekday)
    }
}

class MalayalamDateTest {
    @Test fun `structural validation`() {
        MalayalamDate(1202, MalayalamMonth.Thulam, 1)
        MalayalamDate(1202, MalayalamMonth.Thulam, 32)
        assertFailsWith<IllegalArgumentException> { MalayalamDate(1202, MalayalamMonth.Thulam, 0) }
        assertFailsWith<IllegalArgumentException> { MalayalamDate(1202, MalayalamMonth.Thulam, 33) }
        assertFailsWith<IllegalArgumentException> { MalayalamDate(0, MalayalamMonth.Thulam, 1) }
    }

    @Test fun `ordering is by year, then month, then day`() {
        val dates =
            listOf(
                MalayalamDate(1201, MalayalamMonth.Karkidakam, 32),
                MalayalamDate(1202, MalayalamMonth.Chingam, 1),
                MalayalamDate(1202, MalayalamMonth.Chingam, 2),
                MalayalamDate(1202, MalayalamMonth.Kanni, 1),
            )
        assertEquals(dates, dates.shuffled(kotlin.random.Random(7)).sorted())
    }

    @Test fun `value semantics and toString`() {
        assertEquals(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1), MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))
        assertEquals("1202-04-01", MalayalamDate(1202, MalayalamMonth.Vrischikam, 1).toString())
    }
}

class MonthKeyTest {
    @Test fun `arithmetic rolls over years in both directions`() {
        assertEquals(MonthKey(1203, MalayalamMonth.Chingam), MonthKey(1202, MalayalamMonth.Karkidakam).next())
        assertEquals(MonthKey(1201, MalayalamMonth.Karkidakam), MonthKey(1202, MalayalamMonth.Chingam).previous())
        assertEquals(MonthKey(1204, MalayalamMonth.Thulam), MonthKey(1202, MalayalamMonth.Thulam).plus(24))
        assertEquals(MonthKey(1200, MalayalamMonth.Kanni), MonthKey(1202, MalayalamMonth.Thulam).plus(-25))
        assertTrue(MonthKey(1202, MalayalamMonth.Karkidakam) < MonthKey(1203, MalayalamMonth.Chingam))
    }
}

class GeoLocationTest {
    @Test fun `rejects impossible coordinates`() {
        assertFailsWith<IllegalArgumentException> { GeoLocation(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { GeoLocation(0.0, 181.0) }
        assertFailsWith<IllegalArgumentException> { GeoLocation(Double.NaN, 0.0) }
        assertEquals("Thiruvananthapuram", GeoLocation.Thiruvananthapuram.name)
    }
}

class MonthBoundaryTest {
    private fun boundary(
        first: LocalDate,
        last: LocalDate,
    ) = MonthBoundary(
        1202,
        MalayalamMonth.Thulam,
        first,
        last,
        Instant.EPOCH,
        Duration.ZERO,
        BoundarySource.Computed,
    )

    @Test fun `length must be 29 to 32 days`() {
        val first = LocalDate.of(2026, 10, 18)
        assertEquals(29, boundary(first, first.plusDays(28)).length)
        assertEquals(32, boundary(first, first.plusDays(31)).length)
        assertFailsWith<IllegalArgumentException> { boundary(first, first.plusDays(27)) }
        assertFailsWith<IllegalArgumentException> { boundary(first, first.plusDays(32)) }
    }

    @Test fun `contains is inclusive at both ends`() {
        val b = boundary(LocalDate.of(2026, 10, 18), LocalDate.of(2026, 11, 16))
        assertTrue(LocalDate.of(2026, 10, 18) in b)
        assertTrue(LocalDate.of(2026, 11, 16) in b)
        assertFalse(LocalDate.of(2026, 11, 17) in b)
    }
}
