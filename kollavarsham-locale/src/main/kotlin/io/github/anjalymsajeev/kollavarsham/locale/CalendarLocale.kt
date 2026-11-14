package io.github.anjalymsajeev.kollavarsham.locale

/**
 * Display language for names and (from Sprint 3) formatted dates.
 *
 * ```kotlin
 * MalayalamMonth.Vrischikam.displayName(CalendarLocale.MALAYALAM)   // "വൃശ്ചികം"
 * MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH)     // "Vrischikam"
 * ```
 */
public enum class CalendarLocale {
    /** Malayalam script. */
    MALAYALAM,

    /** English transliteration in Latin script. */
    ENGLISH,
}
