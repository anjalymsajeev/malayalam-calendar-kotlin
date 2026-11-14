package io.github.anjalymsajeev.kollavarsham.panchangam

import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.locale.MalayalamTransliterator
import io.github.anjalymsajeev.kollavarsham.locale.Transliteration

/**
 * The 27 nakshatras (birth stars), each spanning 13°20′ of the Moon's sidereal longitude,
 * starting from Ashwathi at 0° (Lahiri).
 *
 * ```kotlin
 * Nakshatra.of(22)                                        // Thiruvonam
 * Nakshatra.Thiruvonam.displayName(CalendarLocale.MALAYALAM) // "തിരുവോണം"
 * ```
 *
 * @property number 1 (Ashwathi) to 27 (Revathi).
 */
public enum class Nakshatra(
    public val number: Int,
    internal val malayalam: String,
) {
    Ashwathi(1, "അശ്വതി"),
    Bharani(2, "ഭരണി"),
    Karthika(3, "കാർത്തിക"),
    Rohini(4, "രോഹിണി"),
    Makayiram(5, "മകയിരം"),
    Thiruvathira(6, "തിരുവാതിര"),
    Punartham(7, "പുണർതം"),
    Pooyam(8, "പൂയം"),
    Ayilyam(9, "ആയില്യം"),
    Makam(10, "മകം"),
    Pooram(11, "പൂരം"),
    Uthram(12, "ഉത്രം"),
    Atham(13, "അത്തം"),
    Chithira(14, "ചിത്തിര"),
    Chothi(15, "ചോതി"),
    Vishakham(16, "വിശാഖം"),
    Anizham(17, "അനിഴം"),
    Thrikketta(18, "തൃക്കേട്ട"),
    Moolam(19, "മൂലം"),
    Pooradam(20, "പൂരാടം"),
    Uthradam(21, "ഉത്രാടം"),
    Thiruvonam(22, "തിരുവോണം"),
    Avittam(23, "അവിട്ടം"),
    Chathayam(24, "ചതയം"),
    Pooruruttathi(25, "പൂരുരുട്ടാതി"),
    Uthrattathi(26, "ഉത്രട്ടാതി"),
    Revathi(27, "രേവതി"),
    ;

    /** The following nakshatra; [Revathi] wraps to [Ashwathi]. */
    public val next: Nakshatra get() = entries[number % COUNT]

    /** The preceding nakshatra; [Ashwathi] wraps to [Revathi]. */
    public val previous: Nakshatra get() = entries[(number + COUNT - 2) % COUNT]

    public companion object {
        /** Number of nakshatras. */
        public const val COUNT: Int = 27

        /** Arc of one nakshatra in degrees (13°20′). */
        public const val ARC_DEGREES: Double = 360.0 / COUNT

        /**
         * The nakshatra with [number].
         *
         * @throws IllegalArgumentException outside 1..27.
         */
        public fun of(number: Int): Nakshatra {
            require(number in 1..COUNT) { "Nakshatra number must be in 1..27, was $number" }
            return entries[number - 1]
        }

        internal fun atSiderealLongitude(degrees: Double): Nakshatra = entries[((degrees / ARC_DEGREES).toInt()).coerceIn(0, COUNT - 1)]
    }
}

/**
 * The nakshatra's name in [locale]; for English, [transliteration] chooses the spelling.
 *
 * ```kotlin
 * Nakshatra.Thiruvonam.displayName(CalendarLocale.ENGLISH)                           // "Thiruvonam"
 * Nakshatra.Thiruvonam.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919) // "Tiruvōṇaṁ"
 * ```
 */
public fun Nakshatra.displayName(
    locale: CalendarLocale,
    transliteration: Transliteration = Transliteration.COMMON,
): String = localized(malayalam, name, locale, transliteration)

internal fun localized(
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
