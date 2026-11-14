package io.github.anjalymsajeev.kollavarsham.panchangam

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NakshatraOccurrencesTest {
    private val panchangam = Panchangam.create()
    private val zone = KollavarshamCalendar.INDIAN_STANDARD_TIME

    /** Reference starts of each nakshatra, from the independent astronomy-engine fixture. */
    private val referenceStarts: Map<Nakshatra, List<Instant>> =
        javaClass
            .getResourceAsStream("/reference/nakshatra-boundaries-2020-2027.csv")!!
            .bufferedReader()
            .readLines()
            .drop(1)
            .filter { it.isNotBlank() }
            .map { line -> line.split(',').let { (deg, utc) -> deg.toDouble() to Instant.parse(utc) } }
            .groupBy({ (deg, _) -> Nakshatra.entries[(deg / Nakshatra.ARC_DEGREES).roundToInt() % Nakshatra.COUNT] }, { it.second })

    @Test fun `occurrences match every reference start of every nakshatra, 2021-2026`() {
        val from = LocalDate.of(2021, 1, 1)
        val to = LocalDate.of(2026, 12, 31)
        val rangeStart = from.atStartOfDay(zone).toInstant().plus(Duration.ofDays(2))
        val rangeEnd = to.atStartOfDay(zone).toInstant().minus(Duration.ofDays(2))
        for (nakshatra in Nakshatra.entries) {
            val ours = panchangam.nakshatraOccurrences(nakshatra, from, to).map { it.span.start }
            val reference = referenceStarts.getValue(nakshatra).filter { it in rangeStart..rangeEnd }
            val inner = ours.filter { it in rangeStart..rangeEnd }
            assertEquals(reference.size, inner.size, "$nakshatra: count")
            reference.zip(inner).forEach { (r, o) ->
                // The reference scatters by ~5 s; anything near a minute would mean a missed or wrong span.
                assertTrue(abs(Duration.between(r, o).seconds) < 60, "$nakshatra: reference $r, ours $o")
            }
        }
    }

    @Test fun `a year has 13 or 14 occurrences, in order, not overlapping, all overlapping the range`() {
        val from = LocalDate.of(2027, 1, 1)
        val to = LocalDate.of(2027, 12, 31)
        val rangeStart = from.atStartOfDay(zone).toInstant()
        val rangeEnd = to.plusDays(1).atStartOfDay(zone).toInstant()
        for (nakshatra in Nakshatra.entries) {
            val list = panchangam.nakshatraOccurrences(nakshatra, from, to)
            assertTrue(list.size in 13..14, "$nakshatra: ${list.size}")
            list.forEach { o ->
                assertEquals(nakshatra, o.nakshatra)
                assertEquals(nakshatra, o.span.value)
                assertTrue(o.span.end > rangeStart && o.span.start < rangeEnd, "$nakshatra: $o")
                assertEquals(nakshatra, panchangam.nakshatraAt(o.span.start.plusSeconds(60)))
            }
            list.zipWithNext().forEach { (a, b) ->
                val gap = Duration.between(a.span.start, b.span.start)
                // One sidereal month is about 27.3 days; it is never assumed, only checked.
                assertTrue(gap > Duration.ofDays(26) && gap < Duration.ofDays(29), "$nakshatra: gap $gap")
            }
        }
    }

    @Test fun `observance day under the sunrise convention`() {
        // Onam 2026: Thiruvonam at sunrise on 26 August (the festival date).
        val onam = panchangam.nakshatraOccurrences(Nakshatra.Thiruvonam, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 31))
        assertEquals(LocalDate.of(2026, 8, 26), onam.single().observanceDate)
        // Every observance day really has the nakshatra at sunrise.
        panchangam.nakshatraOccurrences(Nakshatra.Atham, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31)).forEach { o ->
            o.observanceDates.forEach { day -> assertEquals(Nakshatra.Atham, panchangam.nakshatraAt(panchangam.sunrise(day)!!), "$day") }
        }
    }

    @Test fun `six nazhika convention reads 2 h 24 min after sunrise`() {
        val list =
            panchangam.nakshatraOccurrences(
                Nakshatra.Atham,
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 12, 31),
                StarDayConvention.SIX_NAZHIKA,
            )
        list.forEach { o ->
            assertEquals(StarDayConvention.SIX_NAZHIKA, o.convention)
            o.observanceDates.forEach { day ->
                assertEquals(Nakshatra.Atham, panchangam.nakshatraAt(panchangam.sunrise(day)!!.plus(Duration.ofMinutes(144))))
            }
        }
        // Nearly every occurrence has exactly one observance day.
        assertTrue(list.count { it.observanceDates.size == 1 } >= list.size - 2)
    }

    @Test fun `range edges, single-day ranges and invalid ranges`() {
        val anyDay = LocalDate.of(2026, 11, 14)
        val star = panchangam.nakshatraAt(anyDay.atTime(12, 0).atZone(zone).toInstant())
        // A one-day range returns the span in force that day, even though it starts or ends outside the day.
        val single = panchangam.nakshatraOccurrences(star, anyDay, anyDay)
        assertEquals(1, single.size)
        assertTrue(single.single().span.start < anyDay.plusDays(1).atStartOfDay(zone).toInstant())
        assertFailsWith<IllegalArgumentException> { panchangam.nakshatraOccurrences(star, anyDay, anyDay.minusDays(1)) }
    }
}
