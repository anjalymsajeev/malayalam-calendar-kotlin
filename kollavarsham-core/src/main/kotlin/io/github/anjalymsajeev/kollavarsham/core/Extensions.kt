package io.github.anjalymsajeev.kollavarsham.core

import java.time.LocalDate

/**
 * This date in the Kollavarsham calendar.
 *
 * ```kotlin
 * LocalDate.of(2026, 11, 14).toMalayalamDate()   // MalayalamDate(1202, Thulam, 28)
 * ```
 *
 * @throws DateOutOfSupportedRangeException outside [KollavarshamCalendar.supportedRange].
 */
public fun LocalDate.toMalayalamDate(calendar: KollavarshamCalendar = KollavarshamCalendar.create()): MalayalamDate =
    calendar.fromGregorian(this)

/**
 * This Kollavarsham date in the Gregorian calendar.
 *
 * ```kotlin
 * MalayalamDate(1202, MalayalamMonth.Vrischikam, 1).toLocalDate()   // 2026-11-17
 * ```
 *
 * @throws InvalidMalayalamDateException if the day does not exist in that month.
 * @throws DateOutOfSupportedRangeException outside the supported range.
 */
public fun MalayalamDate.toLocalDate(calendar: KollavarshamCalendar = KollavarshamCalendar.create()): LocalDate = calendar.toGregorian(this)
