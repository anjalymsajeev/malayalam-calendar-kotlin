package io.github.anjalymsajeev.kollavarsham.sample

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.malayalamWeekday
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalCalendar
import io.github.anjalymsajeev.kollavarsham.festivals.ObservanceProvider
import io.github.anjalymsajeev.kollavarsham.festivals.RuleBasedObservances
import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.locale.DateStyle
import io.github.anjalymsajeev.kollavarsham.locale.displayName
import io.github.anjalymsajeev.kollavarsham.locale.format
import io.github.anjalymsajeev.kollavarsham.panchangam.Panchangam
import io.github.anjalymsajeev.kollavarsham.panchangam.displayName
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** Everything the details card shows for one day, already formatted. Plain Kotlin: unit-testable. */
data class DayDetails(
    val gregorian: String,
    val malayalamDate: String,
    val weekday: String,
    val nakshatra: String,
    val tithi: String,
    val sunrise: String,
    val sunset: String,
    val festivals: List<String>,
    val borderlineMonthNote: Boolean,
)

/**
 * The sample's presentation logic. It only calls public SDK APIs; the Composables render what
 * this returns and contain no calendar logic.
 */
class CalendarPresenter(
    private val calendar: KollavarshamCalendar = KollavarshamCalendar.create(),
    private val panchangam: Panchangam = Panchangam.create(),
    private val observances: ObservanceProvider = RuleBasedObservances(FestivalCalendar.create()),
) {
    private val time = DateTimeFormatter.ofPattern("HH:mm")
    private val gregorianFormat = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun details(
        date: LocalDate,
        locale: CalendarLocale,
    ): DayDetails {
        val malayalam = calendar.fromGregorian(date)
        val day = panchangam.day(date)
        val boundary = calendar.monthBoundary(malayalam.year, malayalam.month)
        return DayDetails(
            gregorian = date.format(gregorianFormat),
            malayalamDate = malayalam.format(locale, DateStyle.LONG),
            weekday = date.malayalamWeekday.displayName(locale),
            nakshatra = day.nakshatraAtSunrise.displayName(locale),
            tithi = "${day.tithiAtSunrise.paksha.displayName(locale)} ${day.tithiAtSunrise.displayName(locale)}",
            sunrise = local(day.sunrise),
            sunset = local(day.sunset),
            festivals = observances.observancesOn(date).map { it.name(locale) },
            borderlineMonthNote = boundary.cutoffMargin.abs().toMinutes() < 10,
        )
    }

    /** Dates in [month] that have at least one observance, for the dots in the grid. */
    fun festivalDates(month: YearMonth): Set<LocalDate> =
        observances.observances(month.atDay(1)..month.atEndOfMonth()).map { it.date }.toSet()

    /** "Thulam – Vrischikam 1202" style title for the Malayalam months a Gregorian month spans. */
    fun malayalamMonthsTitle(
        month: YearMonth,
        locale: CalendarLocale,
    ): String {
        val first = calendar.fromGregorian(month.atDay(1))
        val last = calendar.fromGregorian(month.atEndOfMonth())
        val names = listOf(first, last).map { it.month.displayName(locale) }.distinct().joinToString(" – ")
        val years = listOf(first.year, last.year).distinct().joinToString("–")
        return "$names $years"
    }

    private fun local(instant: Instant): String = instant.atZone(KollavarshamCalendar.INDIAN_STANDARD_TIME).format(time)
}
