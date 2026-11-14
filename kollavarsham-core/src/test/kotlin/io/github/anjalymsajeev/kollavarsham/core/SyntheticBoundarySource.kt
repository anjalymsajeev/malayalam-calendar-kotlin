package io.github.anjalymsajeev.kollavarsham.core

import io.github.anjalymsajeev.kollavarsham.core.boundary.ComputedMonthStart
import io.github.anjalymsajeev.kollavarsham.core.boundary.MonthBoundarySource
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

/**
 * A deterministic, table-driven stand-in for the astronomical source (Sprint 2).
 *
 * Starts at Chingam 1 of ME 1070 = 1894-08-17. Ordinary years use [BASE_LENGTHS] (365 days);
 * every fourth year Kanni has 32 days (366 days). The values are realistic in shape but are
 * NOT real calendar data: they only exercise the conversion mechanics.
 */
internal class SyntheticBoundarySource : MonthBoundarySource {
    val calls = AtomicInteger()

    private val starts: Map<MonthKey, LocalDate> =
        buildMap {
            var key = MonthKey(FIRST_YEAR, MalayalamMonth.Chingam)
            var day = LocalDate.of(1894, 8, 17)
            while (key.year <= LAST_YEAR) {
                put(key, day)
                day = day.plusDays(lengthOf(key).toLong())
                key = key.next()
            }
        }

    override fun monthStart(key: MonthKey): ComputedMonthStart {
        calls.incrementAndGet()
        val first = starts[key] ?: error("Synthetic table has no $key")
        return ComputedMonthStart(first, first.atTime(6, 0).toInstant(ZoneOffset.UTC), Duration.ofMinutes(60))
    }

    companion object {
        const val FIRST_YEAR = 1070
        const val LAST_YEAR = 1280
        private val BASE_LENGTHS = intArrayOf(31, 31, 30, 29, 30, 29, 30, 30, 31, 31, 32, 31)

        fun lengthOf(key: MonthKey): Int =
            BASE_LENGTHS[key.month.ordinal] + if (key.month == MalayalamMonth.Kanni && key.year % 4 == 0) 1 else 0

        fun calendar(
            configuration: CalendarConfiguration = CalendarConfiguration(),
            verified: Map<MonthKey, LocalDate> = emptyMap(),
        ): KollavarshamCalendar = KollavarshamCalendar(configuration, SyntheticBoundarySource(), verified)
    }
}
