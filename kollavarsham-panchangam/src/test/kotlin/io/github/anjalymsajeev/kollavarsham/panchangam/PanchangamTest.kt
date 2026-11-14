package io.github.anjalymsajeev.kollavarsham.panchangam

import io.github.anjalymsajeev.kollavarsham.core.GeoLocation
import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.locale.Transliteration
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun reference(name: String): List<Pair<Double, Instant>> =
    PanchangamTest::class.java
        .getResourceAsStream("/reference/$name")!!
        .bufferedReader()
        .readLines()
        .drop(1)
        .filter { it.isNotBlank() }
        .map { line -> line.split(',').let { it[0].toDouble() to Instant.parse(it[1]) } }

class PanchangamTest {
    private val panchangam = Panchangam.create()

    /**
     * Compares our span boundaries with the independent reference. Returns (mean, standard
     * deviation, worst deviation from the mean) in seconds.
     */
    private fun <T> compare(
        rows: List<Pair<Double, Instant>>,
        span: (Instant) -> Span<T>,
    ): Triple<Double, Double, Double> {
        val diffs = mutableListOf<Double>()
        rows.forEach { (_, expected) ->
            // The span that starts at the boundary: probe just after it.
            val s = span(expected.plusSeconds(120))
            diffs += Duration.between(expected, s.start).toMillis() / 1000.0
        }
        val mean = diffs.average()
        val sd = sqrt(diffs.sumOf { (it - mean) * (it - mean) } / diffs.size)
        val worst = diffs.maxOf { kotlin.math.abs(it - mean) }
        println(
            "  ${rows.size} boundaries: mean ${"%.1f".format(
                mean,
            )} s, sd ${"%.1f".format(sd)} s, worst deviation ${"%.1f".format(worst)} s",
        )
        return Triple(mean, sd, worst)
    }

    /*
     * The reference's MoonPhase uses the Sun's geometric longitude, without the 20.5" annual
     * aberration; we use apparent longitudes, the standard definition of new moon (Meeus ch. 49).
     * 20.5" at the mean elongation rate (12.19°/day) is 40.4 s, so our boundaries are expected to
     * be about 40 s earlier, with only a few seconds of scatter.
     */
    @Test fun `tithi boundaries 2020-2027 agree with the independent reference`() {
        val (mean, sd, worst) = compare(reference("tithi-boundaries-2020-2027.csv"), panchangam::tithiSpan)
        assertTrue(mean in -46.0..-35.0, "mean offset $mean s (expected about -40 s, the aberration convention)")
        assertTrue(sd < 10, "sd $sd s")
        assertTrue(worst < 30, "worst deviation $worst s")
    }

    @Test fun `nakshatra boundaries 2020-2027 agree with the independent reference`() {
        val (mean, sd, worst) = compare(reference("nakshatra-boundaries-2020-2027.csv"), panchangam::nakshatraSpan)
        assertTrue(kotlin.math.abs(mean) < 10, "mean offset $mean s")
        assertTrue(sd < 15, "sd $sd s")
        assertTrue(worst < 45, "worst deviation $worst s")
    }

    @Test fun `new and full moons match published times`() {
        // Published geocentric new/full moon instants (UT), to the minute: NASA/USNO eclipse and phase tables.
        val newMoons = listOf("2024-04-08T18:21:00Z", "2025-03-29T10:58:00Z")
        val fullMoons = listOf("2024-09-18T02:34:00Z", "2025-09-07T18:09:00Z")
        newMoons.map(Instant::parse).forEach { t ->
            val span = panchangam.tithiSpan(t.plusSeconds(3600))
            assertEquals(Tithi.ShuklaPrathama, span.value, "$t")
            assertTrue(Duration.between(t, span.start).abs() < Duration.ofMinutes(2), "new moon $t vs ${span.start}")
        }
        fullMoons.map(Instant::parse).forEach { t ->
            val span = panchangam.tithiSpan(t.plusSeconds(3600))
            assertEquals(Tithi.KrishnaPrathama, span.value, "$t")
            assertTrue(Duration.between(t, span.start).abs() < Duration.ofMinutes(2), "full moon $t vs ${span.start}")
        }
    }

    @Test fun `Thiruvonam is the sunrise nakshatra on Onam`() {
        // Thiruvonam (Onam) dates in Kerala; the festival is the day with Thiruvonam at sunrise in Chingam.
        listOf(LocalDate.of(2023, 8, 29), LocalDate.of(2024, 9, 15), LocalDate.of(2025, 9, 5)).forEach { date ->
            assertEquals(Nakshatra.Thiruvonam, panchangam.day(date).nakshatraAtSunrise, "$date")
        }
    }

    @Test fun `spans are contiguous and contain their probe`() {
        val t = Instant.parse("2026-11-14T06:00:00Z")
        val n = panchangam.nakshatraSpan(t)
        assertTrue(t in n)
        assertEquals(n.value, panchangam.nakshatraAt(t))
        val nextN = panchangam.nakshatraSpan(n.end)
        assertEquals(n.end, nextN.start)
        assertEquals(n.value.next, nextN.value)
        val d = Duration.between(n.start, n.end)
        assertTrue(d > Duration.ofHours(19) && d < Duration.ofHours(28), "nakshatra lasts $d")

        val tt = panchangam.tithiSpan(t)
        assertTrue(t in tt)
        assertEquals(tt.value.next, panchangam.tithiSpan(tt.end).value)
        val td = Duration.between(tt.start, tt.end)
        assertTrue(td > Duration.ofHours(19) && td < Duration.ofHours(28), "tithi lasts $td")
    }

    @Test fun `day lists every element between sunrise and next sunrise`() {
        val day = panchangam.day(LocalDate.of(2026, 11, 14))
        assertTrue(day.sunrise < day.sunset && day.sunset < day.nextSunrise)
        assertEquals(day.nakshatraAtSunrise, day.nakshatras.first().value)
        assertEquals(day.tithiAtSunrise, day.tithis.first().value)
        assertTrue(day.nakshatras.first().start <= day.sunrise && day.nakshatras.last().end >= day.nextSunrise)
        day.nakshatras.zipWithNext().forEach { (a, b) -> assertEquals(a.end, b.start) }
        day.tithis.zipWithNext().forEach { (a, b) -> assertEquals(a.end, b.start) }
    }

    @Test fun `location and factory`() {
        assertSame(panchangam, Panchangam.create())
        val kozhikode = Panchangam.create(PanchangamLocation(GeoLocation(11.2588, 75.7804, "Kozhikode")))
        assertNotSame(panchangam, kozhikode)
        // Further west, the Sun rises later in IST.
        val date = LocalDate.of(2026, 11, 14)
        assertTrue(kozhikode.sunrise(date)!! > panchangam.sunrise(date)!!)
        // Nakshatra and tithi are geocentric: the same instant gives the same result anywhere.
        val t = Instant.parse("2026-11-14T06:00:00Z")
        assertEquals(panchangam.nakshatraSpan(t), kozhikode.nakshatraSpan(t))
        assertFailsWith<IllegalStateException> {
            Panchangam.create(PanchangamLocation(GeoLocation(80.0, 0.0))).day(LocalDate.of(2026, 12, 21))
        }
    }
}

class PanchangamNamesTest {
    @Test fun `nakshatra numbering and navigation`() {
        assertEquals(27, Nakshatra.entries.size)
        Nakshatra.entries.forEach { assertEquals(it, Nakshatra.of(it.number)) }
        assertEquals(Nakshatra.Ashwathi, Nakshatra.Revathi.next)
        assertEquals(Nakshatra.Revathi, Nakshatra.Ashwathi.previous)
        assertFailsWith<IllegalArgumentException> { Nakshatra.of(28) }
    }

    @Test fun `tithi numbering, paksha and day of paksha`() {
        assertEquals(30, Tithi.entries.size)
        Tithi.entries.forEach { assertEquals(it, Tithi.of(it.number)) }
        assertEquals(Paksha.Shukla, Tithi.ShuklaPournami.paksha)
        assertEquals(15, Tithi.ShuklaPournami.dayOfPaksha)
        assertEquals(Paksha.Krishna, Tithi.KrishnaEkadashi.paksha)
        assertEquals(11, Tithi.KrishnaEkadashi.dayOfPaksha)
        assertEquals(Tithi.ShuklaPrathama, Tithi.KrishnaAmavasya.next)
        assertFailsWith<IllegalArgumentException> { Tithi.of(0) }
    }

    @Test fun `names in Malayalam and English`() {
        assertEquals("തിരുവോണം", Nakshatra.Thiruvonam.displayName(CalendarLocale.MALAYALAM))
        assertEquals("Thiruvonam", Nakshatra.Thiruvonam.displayName(CalendarLocale.ENGLISH))
        assertEquals("Tiruvōṇaṁ", Nakshatra.Thiruvonam.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919))
        assertEquals("ഏകാദശി", Tithi.KrishnaEkadashi.displayName(CalendarLocale.MALAYALAM))
        assertEquals("Amavasya", Tithi.KrishnaAmavasya.displayName(CalendarLocale.ENGLISH))
        assertEquals("പൗർണ്ണമി", Tithi.ShuklaPournami.displayName(CalendarLocale.MALAYALAM))
        assertEquals("ശുക്ലപക്ഷം", Paksha.Shukla.displayName(CalendarLocale.MALAYALAM))
        val all =
            Nakshatra.entries.map { it.displayName(CalendarLocale.MALAYALAM) } +
                Tithi.entries.map { it.displayName(CalendarLocale.MALAYALAM) }
        all.forEach { name ->
            assertTrue(Normalizer.isNormalized(name, Normalizer.Form.NFC), name)
            assertTrue(name.all { it in 'ഀ'..'ൿ' }, name)
        }
        assertEquals(
            27,
            Nakshatra.entries
                .map { it.displayName(CalendarLocale.MALAYALAM) }
                .toSet()
                .size,
        )
    }
}
