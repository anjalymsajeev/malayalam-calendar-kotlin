package sample

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.core.toMalayalamDate
import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.locale.DateStyle
import io.github.anjalymsajeev.kollavarsham.locale.KollavarshamDateFormatter
import io.github.anjalymsajeev.kollavarsham.locale.NumeralSystem
import io.github.anjalymsajeev.kollavarsham.locale.format
import java.time.Duration
import java.time.LocalDate

fun main() {
    val calendar = KollavarshamCalendar.create()

    val today = LocalDate.of(2026, 11, 14).toMalayalamDate()
    println("Malayalam date:  " + today.format(CalendarLocale.MALAYALAM, DateStyle.FULL, calendar))
    println("English:         " + today.format(CalendarLocale.ENGLISH, DateStyle.FULL, calendar))
    println("Malayalam digits: " + today.format(CalendarLocale.MALAYALAM, DateStyle.LONG, numerals = NumeralSystem.MALAYALAM))

    val mandalam = calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))
    println("Vrischikam 1, 1202 = $mandalam")

    println("\nMonths of ME 1202:")
    val formatter = KollavarshamDateFormatter.ofPattern("MMMM", CalendarLocale.MALAYALAM)
    for (month in calendar.monthsOf(1202)) {
        val flag = if (month.cutoffMargin.abs() < Duration.ofMinutes(10)) "  (borderline)" else ""
        println("  %-10s %s  %s .. %s  %d days$flag".format(month.month, formatter.format(MalayalamDate(1202, month.month, 1)), month.firstDay, month.lastDay, month.length))
    }
}
