package io.github.anjalymsajeev.kollavarsham.core

/**
 * The twelve solar months of the Kollavarsham calendar, in Chingam-first order.
 *
 * Each month corresponds to the Sun's transit through one sidereal rashi. Month lengths vary
 * between 29 and 32 days from year to year, so use [KollavarshamCalendar.monthLength] rather
 * than assuming a fixed length.
 *
 * Display names (Malayalam script and English transliteration) live in `kollavarsham-locale`.
 *
 * ```kotlin
 * val month = MalayalamMonth.of(4)          // MalayalamMonth.Vrischikam
 * month.next                                // MalayalamMonth.Dhanu
 * MalayalamMonth.Chingam.previous           // MalayalamMonth.Karkidakam
 * ```
 *
 * @property number position in the year, 1 (Chingam) to 12 (Karkidakam).
 */
public enum class MalayalamMonth(public val number: Int) {
    /** Simha rashi; begins mid-August and starts the year. */
    Chingam(1),

    /** Kanya rashi; mid-September. */
    Kanni(2),

    /** Tula rashi; mid-October. */
    Thulam(3),

    /** Vrischika rashi; mid-November. */
    Vrischikam(4),

    /** Dhanu rashi; mid-December. */
    Dhanu(5),

    /** Makara rashi; mid-January. */
    Makaram(6),

    /** Kumbha rashi; mid-February. */
    Kumbham(7),

    /** Meena rashi; mid-March. */
    Meenam(8),

    /** Mesha rashi; mid-April (Vishu). */
    Medam(9),

    /** Vrishabha rashi; mid-May. */
    Edavam(10),

    /** Mithuna rashi; mid-June. */
    Mithunam(11),

    /** Karka rashi; mid-July; the last month of the year. */
    Karkidakam(12),
    ;

    /** The following month. [Karkidakam] wraps to [Chingam]. */
    public val next: MalayalamMonth get() = entries[number % MONTHS_PER_YEAR]

    /** The preceding month. [Chingam] wraps to [Karkidakam]. */
    public val previous: MalayalamMonth get() = entries[(number + MONTHS_PER_YEAR - 2) % MONTHS_PER_YEAR]

    public companion object {
        /** Number of months in a Kollavarsham year. */
        public const val MONTHS_PER_YEAR: Int = 12

        /**
         * Returns the month with the given [number].
         *
         * @throws IllegalArgumentException if [number] is not in 1..12.
         */
        public fun of(number: Int): MalayalamMonth {
            require(number in 1..MONTHS_PER_YEAR) { "Malayalam month number must be in 1..12, was $number" }
            return entries[number - 1]
        }
    }
}
