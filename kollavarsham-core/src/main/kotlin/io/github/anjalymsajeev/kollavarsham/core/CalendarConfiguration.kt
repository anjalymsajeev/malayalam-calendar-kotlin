package io.github.anjalymsajeev.kollavarsham.core

import java.time.LocalDate

/**
 * Configuration of a [KollavarshamCalendar].
 *
 * ```kotlin
 * val config = CalendarConfiguration(
 *     referenceLocation = GeoLocation(11.2588, 75.7804, "Kozhikode"),
 *     monthStartOverrides = mapOf(MonthKey(1197, MalayalamMonth.Thulam) to LocalDate.of(2021, 10, 17)),
 * )
 * ```
 *
 * @property referenceLocation location whose sunrise and sunset decide month starts.
 * @property monthStartOverrides caller-supplied first days of months. They take precedence over
 *   both computation and the bundled verified overrides. Each override must keep the affected
 *   months 29..32 days long, or calendar construction fails.
 * @property applyVerifiedOverrides whether to apply the SDK's bundled, evidence-backed almanac
 *   corrections (see `VALIDATION_STRATEGY.md`).
 */
public data class CalendarConfiguration(
    val referenceLocation: GeoLocation = GeoLocation.Thiruvananthapuram,
    val monthStartOverrides: Map<MonthKey, LocalDate> = emptyMap(),
    val applyVerifiedOverrides: Boolean = true,
)

/**
 * Identifies one month of one Kollavarsham year.
 *
 * ```kotlin
 * val key = MonthKey(1202, MalayalamMonth.Vrischikam)
 * key.next()        // MonthKey(1202, Dhanu)
 * ```
 */
public data class MonthKey(
    val year: Int,
    val month: MalayalamMonth,
) : Comparable<MonthKey> {
    /** Months since the start of ME year 0, used for ordering and arithmetic. */
    internal val index: Long get() = year.toLong() * MalayalamMonth.MONTHS_PER_YEAR + month.ordinal

    /** The following month, rolling over to Chingam of the next year after Karkidakam. */
    public fun next(): MonthKey = plus(1)

    /** The preceding month, rolling back to Karkidakam of the previous year before Chingam. */
    public fun previous(): MonthKey = plus(-1)

    /** The month [months] months later (or earlier, if negative). */
    public fun plus(months: Long): MonthKey = fromIndex(index + months)

    override fun compareTo(other: MonthKey): Int = index.compareTo(other.index)

    internal companion object {
        fun fromIndex(index: Long): MonthKey {
            val year = Math.floorDiv(index, MalayalamMonth.MONTHS_PER_YEAR.toLong())
            val month = Math.floorMod(index, MalayalamMonth.MONTHS_PER_YEAR.toLong()).toInt()
            return MonthKey(Math.toIntExact(year), MalayalamMonth.entries[month])
        }
    }
}
