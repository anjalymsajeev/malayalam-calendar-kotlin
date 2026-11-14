package io.github.anjalymsajeev.kollavarsham.panchangam

import java.time.Instant
import java.time.LocalDate

/**
 * A value that holds from [start] (inclusive) to [end] (exclusive), for example one nakshatra.
 *
 * ```kotlin
 * val span = panchangam.nakshatraSpan(Instant.now())
 * println("${span.value} until ${span.end}")
 * ```
 */
public data class Span<out T>(
    val value: T,
    val start: Instant,
    val end: Instant,
) {
    init {
        require(start.isBefore(end)) { "Span start $start must be before end $end" }
    }

    /** Whether [instant] lies within this span. */
    public operator fun contains(instant: Instant): Boolean = !instant.isBefore(start) && instant.isBefore(end)
}

/**
 * Supplies the nakshatra at any moment.
 *
 * These provider interfaces exist so that observance rules (Sprint 5) can be tested with fakes,
 * and so that alternative calculations (for example a traditional Surya Siddhanta panchangam)
 * can be supplied later. [Panchangam] implements all three.
 */
public interface NakshatraProvider {
    /** The nakshatra at [instant]. */
    public fun nakshatraAt(instant: Instant): Nakshatra

    /** The nakshatra at [instant], with the moments it begins and ends. */
    public fun nakshatraSpan(instant: Instant): Span<Nakshatra>
}

/** Supplies the tithi at any moment. */
public interface TithiProvider {
    /** The tithi at [instant]. */
    public fun tithiAt(instant: Instant): Tithi

    /** The tithi at [instant], with the moments it begins and ends. */
    public fun tithiSpan(instant: Instant): Span<Tithi>
}

/** Supplies sunrise and sunset for civil dates at a configured location. */
public interface SunTimesProvider {
    /** Sunrise on [date], or `null` if the Sun does not rise (polar regions). */
    public fun sunrise(date: LocalDate): Instant?

    /** Sunset on [date], or `null` if the Sun does not set. */
    public fun sunset(date: LocalDate): Instant?
}
