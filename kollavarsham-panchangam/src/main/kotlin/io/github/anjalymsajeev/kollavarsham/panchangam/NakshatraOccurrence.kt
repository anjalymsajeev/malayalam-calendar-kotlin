package io.github.anjalymsajeev.kollavarsham.panchangam

import java.time.Duration
import java.time.LocalDate

/**
 * Which civil day "belongs" to a nakshatra, for example a person's birth-star day (Pirannal),
 * when the nakshatra runs across two days. Conventions differ between families and almanacs.
 *
 * ```kotlin
 * panchangam.nakshatraOccurrences(Nakshatra.Atham, from, to, StarDayConvention.SIX_NAZHIKA)
 * ```
 *
 * @property checkpointAfterSunrise the moment after local sunrise at which the nakshatra is read.
 */
public enum class StarDayConvention(
    public val checkpointAfterSunrise: Duration,
) {
    /** The day on which the nakshatra is in force at sunrise. The common Kerala practice. */
    SUNRISE(Duration.ZERO),

    /** The day on which the nakshatra is still in force 6 nazhika (2 h 24 min) after sunrise. */
    SIX_NAZHIKA(Duration.ofMinutes(144)),
}

/**
 * One continuous period during which the Moon is in [nakshatra].
 *
 * @property nakshatra the nakshatra.
 * @property span when it begins and ends. A span lasts roughly 19 to 27 hours, so it usually covers parts
 *   of two civil days.
 * @property convention the convention used for [observanceDates].
 * @property observanceDates the civil days (in the panchangam's zone) that belong to this occurrence
 *   under [convention]. Usually one day. Empty when the span contains no checkpoint (a short span
 *   between two checkpoints). Two days when a long span contains two checkpoints: the convention
 *   alone does not decide between them, so both are reported.
 */
public data class NakshatraOccurrence(
    val nakshatra: Nakshatra,
    val span: Span<Nakshatra>,
    val convention: StarDayConvention,
    val observanceDates: List<LocalDate>,
) {
    /** The first observance day, or `null` if there is none. See [observanceDates]. */
    public val observanceDate: LocalDate? get() = observanceDates.firstOrNull()
}
