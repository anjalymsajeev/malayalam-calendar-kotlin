package io.github.anjalymsajeev.kollavarsham.festivals

import io.github.anjalymsajeev.kollavarsham.core.CalendarConfiguration
import io.github.anjalymsajeev.kollavarsham.core.GeoLocation
import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.panchangam.Panchangam
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Measures the costs an app sees. The numbers are printed for the release notes; the assertions
 * are deliberately loose (about 10x the measured values) so that they catch regressions, not slow
 * CI machines.
 */
class PerformanceTest {
    private fun <T> timeMs(block: () -> T): Pair<T, Double> {
        val start = System.nanoTime()
        val result = block()
        return result to (System.nanoTime() - start) / 1e6
    }

    // A non-default location gives a fresh calendar with an empty cache.
    private fun freshCalendar() =
        KollavarshamCalendar.create(CalendarConfiguration(referenceLocation = GeoLocation(8.5241, 76.9366, "fresh-${Random.nextInt()}")))

    @Test fun `conversion, panchangam and festival costs`() {
        val (_, firstConversion) = timeMs { freshCalendar().fromGregorian(LocalDate.of(2026, 11, 14)) }

        val cold = freshCalendar()
        val (_, allBoundaries) = timeMs { (1075..1276).forEach { cold.monthsOf(it) } }

        val days = List(100_000) { LocalDate.of(1900, 1, 1).plusDays(Random(it).nextLong(73_000)) }
        days.take(1_000).forEach(cold::fromGregorian) // warm-up
        val (_, warmTotal) = timeMs { days.forEach(cold::fromGregorian) }
        val warmMicros = warmTotal * 1000 / days.size

        val panchangam = Panchangam.create()
        panchangam.day(LocalDate.of(2025, 1, 1))
        val (_, yearOfDays) = timeMs { (0L until 365L).forEach { panchangam.day(LocalDate.of(2026, 1, 1).plusDays(it)) } }

        val festivals = FestivalCalendar.create()
        festivals.festivalsIn(1200)
        val (_, fiveYears) = timeMs { (1201..1205).forEach { festivals.festivalsIn(it) } }

        val runtime = Runtime.getRuntime()
        System.gc()
        val before = runtime.totalMemory() - runtime.freeMemory()
        val retained = freshCalendar().also { c -> (1075..1276).forEach { c.monthsOf(it) } }
        System.gc()
        val after = runtime.totalMemory() - runtime.freeMemory()
        val cacheKb = (after - before) / 1024
        check(retained.monthsOf(1202).size == 12)

        println(
            """
            PERF first conversion on a new calendar: ${"%.1f".format(firstConversion)} ms
            PERF all month boundaries 1900-2100:      ${"%.0f".format(allBoundaries)} ms
            PERF fromGregorian, warm cache:           ${"%.2f".format(warmMicros)} µs/call
            PERF Panchangam.day:                      ${"%.2f".format(yearOfDays / 365)} ms/day
            PERF festivalsIn(year), 25 festivals:     ${"%.0f".format(fiveYears / 5)} ms/year
            PERF boundary cache for 1900-2100:        ~$cacheKb KB (rough, GC-based)
            """.trimIndent(),
        )
        assertTrue(firstConversion < 1_000, "first conversion $firstConversion ms")
        assertTrue(allBoundaries < 5_000, "all boundaries $allBoundaries ms")
        assertTrue(warmMicros < 50, "warm conversion $warmMicros µs")
        assertTrue(yearOfDays / 365 < 50, "panchangam day ${yearOfDays / 365} ms")
        assertTrue(fiveYears / 5 < 5_000, "festivals ${fiveYears / 5} ms/year")
    }
}
