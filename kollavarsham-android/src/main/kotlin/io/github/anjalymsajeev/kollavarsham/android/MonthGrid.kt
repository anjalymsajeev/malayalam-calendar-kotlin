package io.github.anjalymsajeev.kollavarsham.android

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * The cells of a month calendar: whole weeks covering a Gregorian or Malayalam month, each cell
 * holding both dates. Plain Kotlin with no Android dependency, so it is unit-testable and
 * usable from any UI toolkit; [KollavarshamMonthView] renders it with Compose.
 *
 * ```kotlin
 * val grid = MonthGrid.ofGregorianMonth(YearMonth.of(2026, 11))
 * grid.weeks.size                  // 5 or 6
 * grid.malayalamMonths             // [Thulam 1202, Vrischikam 1202]
 * ```
 *
 * @property weeks rows of exactly seven cells, starting on [firstDayOfWeek].
 * @property firstDayOfWeek the weekday each row starts with.
 */
public class MonthGrid private constructor(
    public val weeks: List<List<Cell>>,
    public val firstDayOfWeek: DayOfWeek,
) {
    /**
     * One day in the grid.
     *
     * @property date the Gregorian date.
     * @property malayalamDate the same day in the Kollavarsham calendar.
     * @property inMonth whether the day belongs to the month shown (cells outside pad the first and last week).
     */
    public data class Cell(
        val date: LocalDate,
        val malayalamDate: MalayalamDate,
        val inMonth: Boolean,
    )

    /** All cells, row by row. */
    public val cells: List<Cell> get() = weeks.flatten()

    /** The distinct Malayalam (year, month) pairs among the in-month cells, in order. */
    public val malayalamMonths: List<Pair<Int, MalayalamMonth>>
        get() = cells.filter { it.inMonth }.map { it.malayalamDate.year to it.malayalamDate.month }.distinct()

    public companion object {
        /**
         * The grid for a Gregorian month, the usual layout of Kerala wall calendars.
         *
         * @throws io.github.anjalymsajeev.kollavarsham.core.DateOutOfSupportedRangeException if the
         *   padded weeks leave the calendar's supported range.
         */
        public fun ofGregorianMonth(
            month: YearMonth,
            calendar: KollavarshamCalendar = KollavarshamCalendar.create(),
            firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
        ): MonthGrid = build(month.atDay(1), month.atEndOfMonth(), calendar, firstDayOfWeek)

        /** The grid for a Malayalam month, from its first to its last day (29–32 days). */
        public fun ofMalayalamMonth(
            year: Int,
            month: MalayalamMonth,
            calendar: KollavarshamCalendar = KollavarshamCalendar.create(),
            firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
        ): MonthGrid {
            val boundary = calendar.monthBoundary(year, month)
            return build(boundary.firstDay, boundary.lastDay, calendar, firstDayOfWeek)
        }

        private fun build(
            first: LocalDate,
            last: LocalDate,
            calendar: KollavarshamCalendar,
            firstDayOfWeek: DayOfWeek,
        ): MonthGrid {
            val start = first.minusDays(((first.dayOfWeek.value - firstDayOfWeek.value + 7) % 7).toLong())
            val lastDayOfWeek = firstDayOfWeek.minus(1)
            val end = last.plusDays(((lastDayOfWeek.value - last.dayOfWeek.value + 7) % 7).toLong())
            val cells =
                generateSequence(start) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(end) }
                    .map { Cell(it, calendar.fromGregorian(it), it in first..last) }
                    .toList()
            return MonthGrid(cells.chunked(7), firstDayOfWeek)
        }
    }
}
