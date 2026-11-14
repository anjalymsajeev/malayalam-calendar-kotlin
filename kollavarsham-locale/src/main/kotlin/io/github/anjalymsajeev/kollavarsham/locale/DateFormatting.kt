package io.github.anjalymsajeev.kollavarsham.locale

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.malayalamWeekday

/**
 * Predefined date styles. The patterns differ by locale because Malayalam conventionally writes
 * year, month, day.
 *
 * | Style  | ENGLISH                  | MALAYALAM                         |
 * |--------|--------------------------|-----------------------------------|
 * | SHORT  | `28/03/1202`             | `28/03/1202`                      |
 * | MEDIUM | `28 Thulam 1202`         | `1202 തുലാം 28`                   |
 * | LONG   | `28 Thulam 1202 ME`      | `കൊല്ലവർഷം 1202 തുലാം 28`         |
 * | FULL   | `Shani, 28 Thulam 1202 ME` | `കൊല്ലവർഷം 1202 തുലാം 28, ശനി`  |
 *
 * FULL contains the weekday, so it needs a [KollavarshamCalendar] to format.
 */
public enum class DateStyle(
    internal val englishPattern: String,
    internal val malayalamPattern: String,
) {
    SHORT("dd/MM/y", "dd/MM/y"),
    MEDIUM("d MMMM y", "y MMMM d"),
    LONG("d MMMM y G", "G y MMMM d"),
    FULL("EEEE, d MMMM y G", "G y MMMM d, EEEE"),
}

/**
 * Formats [MalayalamDate]s. Immutable and thread-safe.
 *
 * Pattern letters:
 *
 * | Pattern | Meaning                    | Example (ENGLISH / MALAYALAM)  |
 * |---------|----------------------------|--------------------------------|
 * | `y`…    | year, at least as many digits as letters | `1202`           |
 * | `M`     | month number               | `3`                            |
 * | `MM`    | month number, 2 digits     | `03`                           |
 * | `MMMM`  | month name                 | `Thulam` / `തുലാം`             |
 * | `d`     | day of month               | `8`                            |
 * | `dd`    | day of month, 2 digits     | `08`                           |
 * | `EEEE`  | weekday name (needs a calendar) | `Shani` / `ശനി`           |
 * | `G`     | era                        | `ME` / `കൊല്ലവർഷം`             |
 * | `'…'`   | literal text; `''` is a single quote |                      |
 *
 * Other ASCII letters are reserved and rejected. All other characters are copied as they are.
 *
 * ```kotlin
 * val formatter = KollavarshamDateFormatter.ofPattern("d MMMM y", CalendarLocale.MALAYALAM, NumeralSystem.MALAYALAM)
 * formatter.format(MalayalamDate(1202, MalayalamMonth.Thulam, 28))   // "൨൮ തുലാം ൧൨൦൨"
 * ```
 */
public class KollavarshamDateFormatter private constructor(
    private val tokens: List<Token>,
    /** The display language. */
    public val locale: CalendarLocale,
    /** The digits used for numbers. */
    public val numerals: NumeralSystem,
    /** Latin spelling of names for [CalendarLocale.ENGLISH]. */
    public val transliteration: Transliteration,
) {
    /** Whether the pattern contains the weekday, which requires [format] with a calendar. */
    public val needsWeekday: Boolean = tokens.any { it is Token.Weekday }

    /**
     * Formats [date] without weekday information.
     *
     * @throws IllegalStateException if the pattern contains `EEEE`; use the overload with a calendar.
     */
    public fun format(date: MalayalamDate): String {
        check(!needsWeekday) { "Pattern contains EEEE (weekday); call format(date, calendar)" }
        return render(date, null)
    }

    /**
     * Formats [date], using [calendar] to work out the weekday.
     *
     * @throws io.github.anjalymsajeev.kollavarsham.core.InvalidMalayalamDateException if the date does not exist.
     */
    public fun format(
        date: MalayalamDate,
        calendar: KollavarshamCalendar,
    ): String = render(date, if (needsWeekday) calendar.toGregorian(date).malayalamWeekday.displayName(locale, transliteration) else null)

    /** A copy using [numerals]. */
    public fun withNumerals(numerals: NumeralSystem): KollavarshamDateFormatter =
        KollavarshamDateFormatter(tokens, locale, numerals, transliteration)

    /** A copy using [transliteration]. */
    public fun withTransliteration(transliteration: Transliteration): KollavarshamDateFormatter =
        KollavarshamDateFormatter(tokens, locale, numerals, transliteration)

    private fun render(
        date: MalayalamDate,
        weekday: String?,
    ): String =
        buildString {
            for (token in tokens) {
                when (token) {
                    is Token.Literal -> append(token.text)
                    is Token.Year -> append(MalayalamNumerals.format(date.year, numerals, token.minDigits))
                    is Token.MonthNumber -> append(MalayalamNumerals.format(date.month.number, numerals, token.minDigits))
                    Token.MonthName -> append(date.month.displayName(locale, transliteration))
                    is Token.Day -> append(MalayalamNumerals.format(date.day, numerals, token.minDigits))
                    Token.Weekday -> append(weekday)
                    Token.Era -> append(if (locale == CalendarLocale.MALAYALAM) "കൊല്ലവർഷം" else "ME")
                }
            }
        }

    internal sealed interface Token {
        data class Literal(
            val text: String,
        ) : Token

        data class Year(
            val minDigits: Int,
        ) : Token

        data class MonthNumber(
            val minDigits: Int,
        ) : Token

        data object MonthName : Token

        data class Day(
            val minDigits: Int,
        ) : Token

        data object Weekday : Token

        data object Era : Token
    }

    public companion object {
        /**
         * A formatter for [pattern]; see the class documentation for the letters.
         *
         * @throws IllegalArgumentException if the pattern is invalid.
         */
        public fun ofPattern(
            pattern: String,
            locale: CalendarLocale = CalendarLocale.ENGLISH,
            numerals: NumeralSystem = NumeralSystem.WESTERN,
            transliteration: Transliteration = Transliteration.COMMON,
        ): KollavarshamDateFormatter = KollavarshamDateFormatter(parse(pattern), locale, numerals, transliteration)

        /**
         * A formatter for a predefined [style] in [locale].
         *
         * ```kotlin
         * KollavarshamDateFormatter.ofStyle(DateStyle.LONG, CalendarLocale.MALAYALAM)
         *     .format(MalayalamDate(1202, MalayalamMonth.Thulam, 28))   // "കൊല്ലവർഷം 1202 തുലാം 28"
         * ```
         */
        public fun ofStyle(
            style: DateStyle,
            locale: CalendarLocale,
            numerals: NumeralSystem = NumeralSystem.WESTERN,
            transliteration: Transliteration = Transliteration.COMMON,
        ): KollavarshamDateFormatter =
            ofPattern(
                if (locale == CalendarLocale.MALAYALAM) style.malayalamPattern else style.englishPattern,
                locale,
                numerals,
                transliteration,
            )

        private fun parse(pattern: String): List<Token> {
            val tokens = mutableListOf<Token>()
            var i = 0
            while (i < pattern.length) {
                val c = pattern[i]
                if (c == '\'') {
                    // '' outside quotes is a quote; inside quoted text, '' is also a quote (as in java.time).
                    if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                        tokens += Token.Literal("'")
                        i += 2
                        continue
                    }
                    val text = StringBuilder()
                    var j = i + 1
                    while (true) {
                        require(j < pattern.length) { "Unterminated quote at index $i in \"$pattern\"" }
                        if (pattern[j] == '\'') {
                            if (j + 1 < pattern.length && pattern[j + 1] == '\'') {
                                text.append('\'')
                                j += 2
                                continue
                            }
                            break
                        }
                        text.append(pattern[j])
                        j++
                    }
                    tokens += Token.Literal(text.toString())
                    i = j + 1
                    continue
                }
                if (c !in 'a'..'z' && c !in 'A'..'Z') {
                    tokens += Token.Literal(c.toString())
                    i++
                    continue
                }
                var count = 1
                while (i + count < pattern.length && pattern[i + count] == c) count++
                tokens +=
                    when {
                        c == 'y' -> Token.Year(count)
                        c == 'M' && count <= 2 -> Token.MonthNumber(count)
                        c == 'M' && count == 4 -> Token.MonthName
                        c == 'd' && count <= 2 -> Token.Day(count)
                        c == 'E' && count == 4 -> Token.Weekday
                        c == 'G' && count == 1 -> Token.Era
                        else -> throw IllegalArgumentException("Unsupported pattern \"${c.toString().repeat(count)}\" in \"$pattern\"")
                    }
                i += count
            }
            return tokens
        }
    }
}

/**
 * Formats this date in a predefined [style]. For [DateStyle.FULL], use the overload with a calendar.
 *
 * ```kotlin
 * val date = MalayalamDate(1202, MalayalamMonth.Thulam, 28)
 * date.format(CalendarLocale.MALAYALAM)                                   // "1202 തുലാം 28"
 * date.format(CalendarLocale.ENGLISH, DateStyle.LONG)                     // "28 Thulam 1202 ME"
 * date.format(CalendarLocale.MALAYALAM, numerals = NumeralSystem.MALAYALAM) // "൧൨൦൨ തുലാം ൨൮"
 * ```
 *
 * @throws IllegalStateException for [DateStyle.FULL], which needs the weekday.
 */
public fun MalayalamDate.format(
    locale: CalendarLocale,
    style: DateStyle = DateStyle.MEDIUM,
    numerals: NumeralSystem = NumeralSystem.WESTERN,
): String = KollavarshamDateFormatter.ofStyle(style, locale, numerals).format(this)

/**
 * Formats this date in [style], using [calendar] for the weekday.
 *
 * ```kotlin
 * MalayalamDate(1202, MalayalamMonth.Thulam, 28)
 *     .format(CalendarLocale.MALAYALAM, DateStyle.FULL, KollavarshamCalendar.create())  // "കൊല്ലവർഷം 1202 തുലാം 28, ശനി"
 * ```
 */
public fun MalayalamDate.format(
    locale: CalendarLocale,
    style: DateStyle,
    calendar: KollavarshamCalendar,
    numerals: NumeralSystem = NumeralSystem.WESTERN,
): String = KollavarshamDateFormatter.ofStyle(style, locale, numerals).format(this, calendar)
