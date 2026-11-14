package io.github.anjalymsajeev.kollavarsham.locale

/**
 * Digits used when formatting numbers.
 *
 * Modern Malayalam text mostly uses Western (Arabic) digits, so that is the default for both
 * locales. Malayalam digits (൦–൯) are available for traditional presentation.
 */
public enum class NumeralSystem {
    /** 0123456789. */
    WESTERN,

    /** ൦൧൨൩൪൫൬൭൮൯ (U+0D66..U+0D6F). */
    MALAYALAM,
}

/**
 * Conversion between Western and Malayalam digits.
 *
 * Only the decimal digits are converted. The traditional Malayalam number signs ൰ (10),
 * ൱ (100) and ൲ (1000) are a different, non-positional system and are not used.
 *
 * ```kotlin
 * MalayalamNumerals.format(1202)              // "൧൨൦൨"
 * MalayalamNumerals.toMalayalamDigits("28/03") // "൨൮/൦൩"
 * MalayalamNumerals.parse("൧൨൦൨")            // 1202
 * ```
 */
public object MalayalamNumerals {
    private const val MALAYALAM_ZERO = '൦'

    /** [value] written with Malayalam digits, for example `-15` becomes `"-൧൫"`. */
    public fun format(value: Long): String = toMalayalamDigits(value.toString())

    /** [value] written with Malayalam digits. */
    public fun format(value: Int): String = format(value.toLong())

    /** Replaces every Western digit in [text] with its Malayalam digit; other characters are unchanged. */
    public fun toMalayalamDigits(text: String): String =
        buildString(text.length) { text.forEach { append(if (it in '0'..'9') MALAYALAM_ZERO + (it - '0') else it) } }

    /** Replaces every Malayalam digit in [text] with its Western digit; other characters are unchanged. */
    public fun toWesternDigits(text: String): String =
        buildString(text.length) { text.forEach { append(if (it in MALAYALAM_ZERO..'൯') '0' + (it - MALAYALAM_ZERO) else it) } }

    /**
     * Parses an integer written with Malayalam or Western digits (or a mix), with an optional sign.
     *
     * @throws NumberFormatException if [text] is not an integer.
     */
    public fun parse(text: String): Long = toWesternDigits(text.trim()).toLong()

    internal fun format(
        value: Int,
        numerals: NumeralSystem,
        minDigits: Int = 1,
    ): String {
        val digits = value.toString().padStart(minDigits, '0')
        return if (numerals == NumeralSystem.MALAYALAM) toMalayalamDigits(digits) else digits
    }
}
