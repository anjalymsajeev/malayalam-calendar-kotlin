package io.github.anjalymsajeev.kollavarsham.locale

import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.core.MalayalamWeekday

// Malayalam strings use atomic chillu letters (U+0D7A..U+0D7F) and are stored in Unicode NFC.

private val MONTH_NAMES_ML: Map<MalayalamMonth, String> =
    mapOf(
        MalayalamMonth.Chingam to "ചിങ്ങം",
        MalayalamMonth.Kanni to "കന്നി",
        MalayalamMonth.Thulam to "തുലാം",
        MalayalamMonth.Vrischikam to "വൃശ്ചികം",
        MalayalamMonth.Dhanu to "ധനു",
        MalayalamMonth.Makaram to "മകരം",
        MalayalamMonth.Kumbham to "കുംഭം",
        MalayalamMonth.Meenam to "മീനം",
        MalayalamMonth.Medam to "മേടം",
        MalayalamMonth.Edavam to "ഇടവം",
        MalayalamMonth.Mithunam to "മിഥുനം",
        MalayalamMonth.Karkidakam to "കർക്കടകം",
    )

private val WEEKDAY_NAMES_ML: Map<MalayalamWeekday, String> =
    mapOf(
        MalayalamWeekday.Njayar to "ഞായർ",
        MalayalamWeekday.Thinkal to "തിങ്കൾ",
        MalayalamWeekday.Chovva to "ചൊവ്വ",
        MalayalamWeekday.Budhan to "ബുധൻ",
        MalayalamWeekday.Vyazham to "വ്യാഴം",
        MalayalamWeekday.Velli to "വെള്ളി",
        MalayalamWeekday.Shani to "ശനി",
    )

/**
 * The month's name in [locale]. For [CalendarLocale.ENGLISH], [transliteration] chooses the
 * Latin spelling; it is ignored for Malayalam.
 *
 * ```kotlin
 * MalayalamMonth.Chingam.displayName(CalendarLocale.MALAYALAM)                         // "ചിങ്ങം"
 * MalayalamMonth.Chingam.displayName(CalendarLocale.ENGLISH)                           // "Chingam"
 * MalayalamMonth.Chingam.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919) // "Ciṅṅaṁ"
 * ```
 */
public fun MalayalamMonth.displayName(
    locale: CalendarLocale,
    transliteration: Transliteration = Transliteration.COMMON,
): String = name(MONTH_NAMES_ML.getValue(this), name, locale, transliteration)

/**
 * The weekday's name in [locale]. For [CalendarLocale.ENGLISH], [transliteration] chooses the
 * Latin spelling of the Malayalam name ("Njayar"), not the English weekday ("Sunday", which
 * `java.time.DayOfWeek` already provides).
 *
 * ```kotlin
 * MalayalamWeekday.Njayar.displayName(CalendarLocale.MALAYALAM)                         // "ഞായർ"
 * MalayalamWeekday.Njayar.displayName(CalendarLocale.ENGLISH)                           // "Njayar"
 * MalayalamWeekday.Njayar.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919) // "Ñāyar"
 * ```
 */
public fun MalayalamWeekday.displayName(
    locale: CalendarLocale,
    transliteration: Transliteration = Transliteration.COMMON,
): String = name(WEEKDAY_NAMES_ML.getValue(this), name, locale, transliteration)

private fun name(
    malayalam: String,
    common: String,
    locale: CalendarLocale,
    transliteration: Transliteration,
): String =
    when (locale) {
        CalendarLocale.MALAYALAM -> malayalam
        CalendarLocale.ENGLISH ->
            when (transliteration) {
                Transliteration.COMMON -> common
                Transliteration.ISO_15919 -> MalayalamTransliterator.toIso15919(malayalam).replaceFirstChar { it.uppercase() }
            }
    }
