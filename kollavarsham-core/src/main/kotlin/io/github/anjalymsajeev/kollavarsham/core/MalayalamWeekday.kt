package io.github.anjalymsajeev.kollavarsham.core

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Days of the week as named in Malayalam, mapped one-to-one to [java.time.DayOfWeek].
 *
 * ```kotlin
 * MalayalamWeekday.of(DayOfWeek.SUNDAY)          // MalayalamWeekday.Njayar
 * LocalDate.of(2026, 11, 14).malayalamWeekday    // MalayalamWeekday.Shani
 * ```
 *
 * @property dayOfWeek the equivalent ISO day of week.
 */
public enum class MalayalamWeekday(
    public val dayOfWeek: DayOfWeek,
) {
    /** Sunday (ഞായർ). */
    Njayar(DayOfWeek.SUNDAY),

    /** Monday (തിങ്കൾ). */
    Thinkal(DayOfWeek.MONDAY),

    /** Tuesday (ചൊവ്വ). */
    Chovva(DayOfWeek.TUESDAY),

    /** Wednesday (ബുധൻ). */
    Budhan(DayOfWeek.WEDNESDAY),

    /** Thursday (വ്യാഴം). */
    Vyazham(DayOfWeek.THURSDAY),

    /** Friday (വെള്ളി). */
    Velli(DayOfWeek.FRIDAY),

    /** Saturday (ശനി). */
    Shani(DayOfWeek.SATURDAY),
    ;

    public companion object {
        /** Returns the Malayalam weekday for the ISO [dayOfWeek]. */
        public fun of(dayOfWeek: DayOfWeek): MalayalamWeekday = entries.first { it.dayOfWeek == dayOfWeek }
    }
}

/** The Malayalam weekday of this date. Weekdays are identical in both calendars. */
public val LocalDate.malayalamWeekday: MalayalamWeekday get() = MalayalamWeekday.of(dayOfWeek)
