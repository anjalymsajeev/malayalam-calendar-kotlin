package io.github.anjalymsajeev.kollavarsham.core

import java.time.LocalDate

/**
 * The result of [KollavarshamCalendar.validate].
 *
 * New subtypes may be added in minor releases, so include an `else` branch when matching:
 *
 * ```kotlin
 * when (val result = calendar.validate(date)) {
 *     DateValidation.Valid -> println("ok")
 *     is DateValidation.DayOutOfRange -> println("${date.month} has only ${result.monthLength} days")
 *     else -> println(result)
 * }
 * ```
 */
public sealed interface DateValidation {
    /** The date exists and is within the supported range. */
    public data object Valid : DateValidation

    /** The day number exceeds this year's length of the month. */
    public data class DayOutOfRange(
        val day: Int,
        val monthLength: Int,
    ) : DateValidation

    /** The date is outside [supportedRange]. */
    public data class OutOfSupportedRange(
        val supportedRange: ClosedRange<LocalDate>,
    ) : DateValidation
}

/** Base class of all exceptions thrown by the SDK for invalid input. */
public sealed class KollavarshamException(
    message: String,
) : IllegalArgumentException(message)

/**
 * Thrown when a [MalayalamDate] does not exist, for example day 31 of a 30-day month.
 *
 * @property validation the reason.
 */
public class InvalidMalayalamDateException(
    public val date: MalayalamDate,
    public val validation: DateValidation,
) : KollavarshamException(
        when (validation) {
            is DateValidation.DayOutOfRange ->
                "$date does not exist: ${date.month} ${date.year} has ${validation.monthLength} days"
            else -> "$date is not valid: $validation"
        },
    )

/**
 * Thrown when a date is outside the range the SDK supports and validates.
 *
 * @property supportedRange the supported Gregorian range.
 */
public class DateOutOfSupportedRangeException(
    description: String,
    public val supportedRange: ClosedRange<LocalDate>,
) : KollavarshamException("$description is outside the supported range $supportedRange")
