package io.github.anjalymsajeev.kollavarsham.core

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** What decided a month's first day. */
public enum class BoundarySource {
    /** The astronomical rule (sankranti before 3/5 of daytime). */
    Computed,

    /** A correction bundled with the SDK, backed by a cited almanac observation. */
    VerifiedOverride,

    /** An override supplied in [CalendarConfiguration.monthStartOverrides]. */
    UserOverride,
}

/**
 * The span of one Malayalam month and the evidence behind its first day.
 *
 * ```kotlin
 * val boundary = calendar.monthBoundary(1202, MalayalamMonth.Vrischikam)
 * boundary.firstDay                    // 2026-11-17
 * boundary.length                      // 29..32
 * if (boundary.cutoffMargin.abs() < Duration.ofMinutes(10)) {
 *     // Almanacs may disagree by a day for this month.
 * }
 * ```
 *
 * @property year Kollavarsham year.
 * @property month the month.
 * @property firstDay Gregorian date of day 1.
 * @property lastDay Gregorian date of the last day.
 * @property sankranti computed moment the Sun entered this month's rashi.
 * @property cutoffMargin the Kerala cut-off (sunrise + 3/5 of daytime) minus [sankranti].
 *   Positive means the sankranti was before the cut-off. Small absolute values mark months on
 *   which almanacs may disagree.
 * @property source what decided [firstDay].
 */
public data class MonthBoundary(
    val year: Int,
    val month: MalayalamMonth,
    val firstDay: LocalDate,
    val lastDay: LocalDate,
    val sankranti: Instant,
    val cutoffMargin: Duration,
    val source: BoundarySource,
) {
    init {
        val days = lastDay.toEpochDay() - firstDay.toEpochDay() + 1
        require(days in MalayalamDate.MIN_DAYS_IN_MONTH..MalayalamDate.MAX_DAYS_IN_MONTH) {
            "A Malayalam month must be ${MalayalamDate.MIN_DAYS_IN_MONTH}..${MalayalamDate.MAX_DAYS_IN_MONTH} days, " +
                "but $month $year spans $days days ($firstDay..$lastDay)"
        }
    }

    /** Number of days in the month, 29..32. */
    public val length: Int get() = (lastDay.toEpochDay() - firstDay.toEpochDay() + 1).toInt()

    /** Whether [date] falls within this month. */
    public operator fun contains(date: LocalDate): Boolean = date in firstDay..lastDay
}
