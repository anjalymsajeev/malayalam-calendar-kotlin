package io.github.anjalymsajeev.kollavarsham.core

import io.github.anjalymsajeev.kollavarsham.core.boundary.BoundaryResolver
import io.github.anjalymsajeev.kollavarsham.core.boundary.MonthBoundarySource
import io.github.anjalymsajeev.kollavarsham.core.boundary.VerifiedOverrides
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Converts between Gregorian and Kollavarsham dates and answers questions about month structure.
 *
 * Instances are immutable and thread-safe; share one per configuration. Month boundaries are
 * computed lazily per Kollavarsham year and cached.
 *
 * ```kotlin
 * val calendar = KollavarshamCalendar.create()                        // available from Sprint 2
 * calendar.fromGregorian(LocalDate.of(2026, 11, 14))                  // 1202 Thulam 28
 * calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))  // 2026-11-17
 * calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))   // DayOutOfRange(31, 30)
 * ```
 *
 * @property configuration the configuration this calendar was created with.
 */
public class KollavarshamCalendar internal constructor(
    public val configuration: CalendarConfiguration,
    source: MonthBoundarySource,
    verifiedOverrides: Map<MonthKey, LocalDate> = VerifiedOverrides.bundled,
) {
    private val boundaries = BoundaryResolver(
        source = source,
        verifiedOverrides = if (configuration.applyVerifiedOverrides) verifiedOverrides else emptyMap(),
        userOverrides = configuration.monthStartOverrides,
    )

    /** Gregorian dates this calendar converts and validates. Outside it, conversions throw. */
    public val supportedRange: ClosedRange<LocalDate> get() = SUPPORTED_RANGE

    // ---------------------------------------------------------------- conversion

    /**
     * Converts a Gregorian [date] to its Kollavarsham date.
     *
     * ```kotlin
     * calendar.fromGregorian(LocalDate.of(2026, 11, 14))   // MalayalamDate(1202, Thulam, 28)
     * ```
     *
     * @throws DateOutOfSupportedRangeException if [date] is outside [supportedRange].
     */
    public fun fromGregorian(date: LocalDate): MalayalamDate {
        if (date !in SUPPORTED_RANGE) throw DateOutOfSupportedRangeException(date.toString(), SUPPORTED_RANGE)
        // Chingam of ME (G - 825) starts in August of G - 1, so the date is never before it.
        var year = date.year - CHINGAM_GREGORIAN_OFFSET - 1
        if (date >= boundaries.boundary(MonthKey(year + 1, MalayalamMonth.Chingam)).firstDay) year += 1
        val month = boundaries.monthsOf(year).last { it.firstDay <= date }
        return MalayalamDate(year, month.month, (date.toEpochDay() - month.firstDay.toEpochDay()).toInt() + 1)
    }

    /**
     * Converts a Kollavarsham [date] to its Gregorian date.
     *
     * ```kotlin
     * calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))   // 2026-11-17
     * ```
     *
     * @throws InvalidMalayalamDateException if the day does not exist in that month.
     * @throws DateOutOfSupportedRangeException if the date is outside [supportedRange].
     */
    public fun toGregorian(date: MalayalamDate): LocalDate =
        when (val validation = validate(date)) {
            DateValidation.Valid -> unchecked(date)
            is DateValidation.OutOfSupportedRange -> throw DateOutOfSupportedRangeException(date.toString(), SUPPORTED_RANGE)
            else -> throw InvalidMalayalamDateException(date, validation)
        }

    /**
     * Like [toGregorian], but returns `null` instead of throwing when [date] is invalid or unsupported.
     *
     * ```kotlin
     * calendar.toGregorianOrNull(MalayalamDate(1202, MalayalamMonth.Thulam, 31))   // null
     * ```
     */
    public fun toGregorianOrNull(date: MalayalamDate): LocalDate? =
        if (validate(date) == DateValidation.Valid) unchecked(date) else null

    // ---------------------------------------------------------------- validation

    /**
     * Checks whether [date] exists and is within [supportedRange]. Never throws.
     *
     * ```kotlin
     * calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))   // DayOutOfRange(31, 30)
     * ```
     */
    public fun validate(date: MalayalamDate): DateValidation {
        if (date.year !in SUPPORTED_YEARS) return DateValidation.OutOfSupportedRange(SUPPORTED_RANGE)
        val boundary = boundaries.boundary(MonthKey(date.year, date.month))
        if (date.day > boundary.length) return DateValidation.DayOutOfRange(date.day, boundary.length)
        if (unchecked(date) !in SUPPORTED_RANGE) return DateValidation.OutOfSupportedRange(SUPPORTED_RANGE)
        return DateValidation.Valid
    }

    /**
     * Whether [date] exists and is within [supportedRange].
     *
     * ```kotlin
     * calendar.isValid(MalayalamDate(1202, MalayalamMonth.Thulam, 30))   // true
     * ```
     */
    public fun isValid(date: MalayalamDate): Boolean = validate(date) == DateValidation.Valid

    // ---------------------------------------------------------------- month structure

    /**
     * The boundaries of one month, including the sankranti and the cut-off margin.
     *
     * ```kotlin
     * calendar.monthBoundary(1202, MalayalamMonth.Vrischikam).firstDay   // 2026-11-17
     * ```
     *
     * @throws DateOutOfSupportedRangeException if [year] is outside the supported years.
     */
    public fun monthBoundary(year: Int, month: MalayalamMonth): MonthBoundary {
        requireSupportedYear(year)
        return boundaries.boundary(MonthKey(year, month))
    }

    /**
     * The number of days (29..32) in [month] of [year].
     *
     * ```kotlin
     * calendar.monthLength(1202, MalayalamMonth.Thulam)   // 30
     * ```
     */
    public fun monthLength(year: Int, month: MalayalamMonth): Int = monthBoundary(year, month).length

    /**
     * All twelve months of [year], Chingam first.
     *
     * ```kotlin
     * calendar.monthsOf(1202).sumOf { it.length }   // 365 or 366
     * ```
     */
    public fun monthsOf(year: Int): List<MonthBoundary> {
        requireSupportedYear(year)
        return boundaries.monthsOf(year)
    }

    // ---------------------------------------------------------------- arithmetic

    /**
     * The date [days] days after [date] (before it, if negative).
     *
     * ```kotlin
     * calendar.plusDays(MalayalamDate(1202, MalayalamMonth.Thulam, 30), 1)   // 1202 Vrischikam 1
     * ```
     *
     * @throws InvalidMalayalamDateException if [date] does not exist.
     * @throws DateOutOfSupportedRangeException if the input or result is outside [supportedRange].
     */
    public fun plusDays(date: MalayalamDate, days: Long): MalayalamDate =
        fromGregorian(toGregorian(date).plusDays(days))

    /**
     * The same day [months] months later (earlier, if negative). The day is clamped to the
     * length of the target month.
     *
     * ```kotlin
     * calendar.plusMonths(MalayalamDate(1201, MalayalamMonth.Chingam, 31), 1)   // 1201 Kanni 30 or 31
     * ```
     *
     * @throws InvalidMalayalamDateException if [date] does not exist.
     * @throws DateOutOfSupportedRangeException if the input or result is outside [supportedRange].
     */
    public fun plusMonths(date: MalayalamDate, months: Long): MalayalamDate {
        toGregorian(date) // validates the input
        val target = MonthKey(date.year, date.month).plus(months)
        if (target.year !in SUPPORTED_YEARS) {
            throw DateOutOfSupportedRangeException("$date plus $months months", SUPPORTED_RANGE)
        }
        val result = MalayalamDate(target.year, target.month, minOf(date.day, monthLength(target.year, target.month)))
        toGregorian(result) // the clamped result must also be in range
        return result
    }

    /**
     * Days from [start] (inclusive) to [endExclusive]. Negative if [endExclusive] is earlier.
     *
     * ```kotlin
     * calendar.daysBetween(MalayalamDate(1202, MalayalamMonth.Chingam, 1), MalayalamDate(1203, MalayalamMonth.Chingam, 1))   // 365 or 366
     * ```
     */
    public fun daysBetween(start: MalayalamDate, endExclusive: MalayalamDate): Long =
        ChronoUnit.DAYS.between(toGregorian(start), toGregorian(endExclusive))

    /**
     * Today's Kollavarsham date according to [clock], which defaults to the system clock in
     * Indian Standard Time.
     *
     * ```kotlin
     * calendar.today()
     * calendar.today(Clock.fixed(Instant.parse("2026-11-14T06:00:00Z"), ZoneOffset.UTC))   // 1202 Thulam 28
     * ```
     */
    public fun today(clock: Clock = Clock.system(INDIAN_STANDARD_TIME)): MalayalamDate = fromGregorian(LocalDate.now(clock))

    // ---------------------------------------------------------------- internals

    private fun unchecked(date: MalayalamDate): LocalDate =
        boundaries.boundary(MonthKey(date.year, date.month)).firstDay.plusDays(date.day - 1L)

    private fun requireSupportedYear(year: Int) {
        if (year !in SUPPORTED_YEARS) throw DateOutOfSupportedRangeException("Kollavarsham year $year", SUPPORTED_RANGE)
    }

    public companion object {
        /** Indian Standard Time, the civil time zone of the Kerala calendar. */
        public val INDIAN_STANDARD_TIME: ZoneId = ZoneId.of("Asia/Kolkata")

        /** Gregorian year minus this equals the ME year from Chingam 1 to 31 December. */
        internal const val CHINGAM_GREGORIAN_OFFSET: Int = 824

        internal val SUPPORTED_RANGE: ClosedRange<LocalDate> = LocalDate.of(1900, 1, 1)..LocalDate.of(2100, 12, 31)

        /** ME years that overlap [SUPPORTED_RANGE]: 1075 (Chingam 1899) to 1276 (Chingam 2100). */
        internal val SUPPORTED_YEARS: IntRange = 1075..1276
    }
}
