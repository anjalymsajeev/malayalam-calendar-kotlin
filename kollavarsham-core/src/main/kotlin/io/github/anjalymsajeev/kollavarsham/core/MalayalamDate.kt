package io.github.anjalymsajeev.kollavarsham.core

/**
 * A date in the Kollavarsham (Malayalam Era, ME) calendar. Immutable and ordered chronologically.
 *
 * The constructor checks **structure only**: [year] must be positive and [day] must be in
 * 1..32, the widest possible month. Whether the day exists in that particular month depends on
 * the astronomically computed month length, so use [KollavarshamCalendar.validate] or
 * [KollavarshamCalendar.toGregorian] to check that.
 *
 * ```kotlin
 * val date = MalayalamDate(year = 1202, month = MalayalamMonth.Vrischikam, day = 1)
 * date < MalayalamDate(1202, MalayalamMonth.Dhanu, 1)   // true
 * MalayalamDate(1202, MalayalamMonth.Thulam, 33)          // throws IllegalArgumentException
 * ```
 *
 * @property year Kollavarsham year, numbered from Chingam (the year starts at Chingam 1).
 * @property month the solar month.
 * @property day day of the month, 1-based.
 */
public data class MalayalamDate(
    val year: Int,
    val month: MalayalamMonth,
    val day: Int,
) : Comparable<MalayalamDate> {

    init {
        require(year >= 1) { "Kollavarsham year must be positive, was $year" }
        require(day in 1..MAX_DAYS_IN_MONTH) { "Day must be in 1..$MAX_DAYS_IN_MONTH, was $day" }
    }

    override fun compareTo(other: MalayalamDate): Int = COMPARATOR.compare(this, other)

    /** ISO-like representation, for example `1202-04-01` for Vrischikam 1, 1202. Not localised. */
    override fun toString(): String =
        "${year.toString().padStart(4, '0')}-${month.number.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    public companion object {
        /** The longest possible Malayalam month. */
        public const val MAX_DAYS_IN_MONTH: Int = 32

        /** The shortest possible Malayalam month. */
        public const val MIN_DAYS_IN_MONTH: Int = 29

        private val COMPARATOR = compareBy<MalayalamDate>({ it.year }, { it.month }, { it.day })
    }
}
