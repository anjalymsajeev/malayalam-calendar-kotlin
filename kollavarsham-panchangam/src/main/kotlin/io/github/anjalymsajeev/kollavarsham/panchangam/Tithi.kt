package io.github.anjalymsajeev.kollavarsham.panchangam

import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.locale.Transliteration

/** The fortnight of the lunar month. */
public enum class Paksha(
    internal val malayalam: String,
) {
    /** Waxing fortnight, from new moon to full moon (വെളുത്ത പക്ഷം). */
    Shukla("ശുക്ലപക്ഷം"),

    /** Waning fortnight, from full moon to new moon (കറുത്ത പക്ഷം). */
    Krishna("കൃഷ്ണപക്ഷം"),
}

/**
 * The 30 tithis (lunar days): each is 12° of the Moon's elongation from the Sun. Tithis 1–15
 * form the [Paksha.Shukla] fortnight ending at full moon ([ShuklaPournami]); 16–30 form
 * [Paksha.Krishna], ending at new moon ([KrishnaAmavasya]).
 *
 * ```kotlin
 * Tithi.of(11)                    // ShuklaEkadashi
 * Tithi.ShuklaEkadashi.paksha     // Shukla
 * Tithi.ShuklaEkadashi.dayOfPaksha // 11
 * ```
 *
 * @property number 1..30.
 */
public enum class Tithi(
    public val number: Int,
) {
    ShuklaPrathama(1),
    ShuklaDwitiya(2),
    ShuklaTritiya(3),
    ShuklaChaturthi(4),
    ShuklaPanchami(5),
    ShuklaShashthi(6),
    ShuklaSaptami(7),
    ShuklaAshtami(8),
    ShuklaNavami(9),
    ShuklaDashami(10),
    ShuklaEkadashi(11),
    ShuklaDwadashi(12),
    ShuklaTrayodashi(13),
    ShuklaChaturdashi(14),
    ShuklaPournami(15),
    KrishnaPrathama(16),
    KrishnaDwitiya(17),
    KrishnaTritiya(18),
    KrishnaChaturthi(19),
    KrishnaPanchami(20),
    KrishnaShashthi(21),
    KrishnaSaptami(22),
    KrishnaAshtami(23),
    KrishnaNavami(24),
    KrishnaDashami(25),
    KrishnaEkadashi(26),
    KrishnaDwadashi(27),
    KrishnaTrayodashi(28),
    KrishnaChaturdashi(29),
    KrishnaAmavasya(30),
    ;

    /** The fortnight. */
    public val paksha: Paksha get() = if (number <= DAYS_PER_PAKSHA) Paksha.Shukla else Paksha.Krishna

    /** Day within the fortnight, 1..15 (15 is Pournami or Amavasya). */
    public val dayOfPaksha: Int get() = (number - 1) % DAYS_PER_PAKSHA + 1

    /** The following tithi; Amavasya wraps to Shukla Prathama. */
    public val next: Tithi get() = entries[number % COUNT]

    public companion object {
        /** Number of tithis in a lunar month. */
        public const val COUNT: Int = 30

        /** Elongation per tithi, degrees. */
        public const val ARC_DEGREES: Double = 12.0

        private const val DAYS_PER_PAKSHA = 15

        /**
         * The tithi with [number].
         *
         * @throws IllegalArgumentException outside 1..30.
         */
        public fun of(number: Int): Tithi {
            require(number in 1..COUNT) { "Tithi number must be in 1..30, was $number" }
            return entries[number - 1]
        }

        internal fun atElongation(degrees: Double): Tithi = entries[((degrees / ARC_DEGREES).toInt()).coerceIn(0, COUNT - 1)]
    }
}

private val TITHI_DAY_NAMES =
    listOf(
        "Prathama" to "പ്രഥമ",
        "Dwitiya" to "ദ്വിതീയ",
        "Tritiya" to "തൃതീയ",
        "Chaturthi" to "ചതുർത്ഥി",
        "Panchami" to "പഞ്ചമി",
        "Shashthi" to "ഷഷ്ഠി",
        "Saptami" to "സപ്തമി",
        "Ashtami" to "അഷ്ടമി",
        "Navami" to "നവമി",
        "Dashami" to "ദശമി",
        "Ekadashi" to "ഏകാദശി",
        "Dwadashi" to "ദ്വാദശി",
        "Trayodashi" to "ത്രയോദശി",
        "Chaturdashi" to "ചതുർദ്ദശി",
    )

/**
 * The tithi's name in [locale], without the fortnight ("Ekadashi", "ഏകാദശി"); day 15 is
 * "Pournami" (പൗർണ്ണമി) or "Amavasya" (അമാവാസി). Combine with [Paksha.displayName] for the full name.
 *
 * ```kotlin
 * Tithi.KrishnaAmavasya.displayName(CalendarLocale.MALAYALAM)   // "അമാവാസി"
 * Tithi.ShuklaEkadashi.displayName(CalendarLocale.ENGLISH)      // "Ekadashi"
 * ```
 */
public fun Tithi.displayName(
    locale: CalendarLocale,
    transliteration: Transliteration = Transliteration.COMMON,
): String {
    val (common, malayalam) =
        when {
            this == Tithi.ShuklaPournami -> "Pournami" to "പൗർണ്ണമി"
            this == Tithi.KrishnaAmavasya -> "Amavasya" to "അമാവാസി"
            else -> TITHI_DAY_NAMES[dayOfPaksha - 1]
        }
    return localized(malayalam, common, locale, transliteration)
}

/**
 * The fortnight's name in [locale].
 *
 * ```kotlin
 * Paksha.Shukla.displayName(CalendarLocale.MALAYALAM)   // "ശുക്ലപക്ഷം"
 * ```
 */
public fun Paksha.displayName(
    locale: CalendarLocale,
    transliteration: Transliteration = Transliteration.COMMON,
): String = localized(malayalam, name, locale, transliteration)
