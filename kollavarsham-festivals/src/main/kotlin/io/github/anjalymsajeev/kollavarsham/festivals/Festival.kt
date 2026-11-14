package io.github.anjalymsajeev.kollavarsham.festivals

import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import java.time.LocalDate

/** Broad grouping of observances, for filtering in apps. */
public enum class ObservanceCategory { HINDU, CHRISTIAN, ISLAMIC, CIVIC, TEMPLE, CUSTOM }

/**
 * A festival defined by a [rule].
 *
 * ```kotlin
 * val onam = KeralaFestivals.ONAM
 * onam.name(CalendarLocale.MALAYALAM)   // "തിരുവോണം"
 * ```
 *
 * @property id stable identifier, for example `"onam"`. Use it for storage and lookups.
 * @property englishName English name.
 * @property malayalamName Malayalam name.
 * @property category grouping.
 * @property rule how the date is found each year.
 */
public data class Festival(
    val id: String,
    val englishName: String,
    val malayalamName: String,
    val category: ObservanceCategory,
    val rule: FestivalRule,
) {
    init {
        require(id.isNotBlank() && id.all { it.isLetterOrDigit() || it == '-' || it == '_' }) { "Invalid festival id \"$id\"" }
    }

    /** The name in [locale]. */
    public fun name(locale: CalendarLocale): String = if (locale == CalendarLocale.MALAYALAM) malayalamName else englishName
}

/** Where an [Observance] came from. */
public enum class ObservanceSource {
    /** Computed from a [FestivalRule]. */
    RULE,

    /** Supplied as a dated entry, for example a government holiday list or a personal event. */
    DATED_ENTRY,
}

/**
 * One occurrence of a festival or observance on a civil date.
 *
 * @property date the Gregorian date it is observed.
 * @property id identifier of the festival or entry.
 */
public data class Observance(
    val date: LocalDate,
    val id: String,
    val englishName: String,
    val malayalamName: String,
    val category: ObservanceCategory,
    val source: ObservanceSource,
) {
    /** The name in [locale]. */
    public fun name(locale: CalendarLocale): String = if (locale == CalendarLocale.MALAYALAM) malayalamName else englishName
}
