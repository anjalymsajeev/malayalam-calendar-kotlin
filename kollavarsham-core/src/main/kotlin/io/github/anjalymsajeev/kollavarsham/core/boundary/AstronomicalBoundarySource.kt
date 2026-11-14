package io.github.anjalymsajeev.kollavarsham.core.boundary

import io.github.anjalymsajeev.kollavarsham.astronomy.Ayanamsa
import io.github.anjalymsajeev.kollavarsham.astronomy.Sankranti
import io.github.anjalymsajeev.kollavarsham.astronomy.SunriseSunset
import io.github.anjalymsajeev.kollavarsham.core.GeoLocation
import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MonthKey
import java.time.Duration
import java.time.LocalDate

/**
 * Month starts from astronomy and the Kerala rule (CALENDAR_ALGORITHM.md §2):
 * day 1 is the IST civil day of the nirayana sankranti if the sankranti falls before
 * sunrise + 3/5 × (sunset − sunrise) at [location] on that day; otherwise the next day.
 */
internal class AstronomicalBoundarySource(
    private val location: GeoLocation,
    private val ayanamsa: Ayanamsa,
) : MonthBoundarySource {

    override fun monthStart(key: MonthKey): ComputedMonthStart {
        // Chingam (ordinal 0) is Simha (rashi 4); Medam (ordinal 8) is Mesha (rashi 0).
        val rashi = (key.month.ordinal + 4) % 12
        val sankranti = Sankranti.find(rashi, estimate(key, rashi).atStartOfDay(KollavarshamCalendar.INDIAN_STANDARD_TIME).toInstant(), ayanamsa)

        val day = sankranti.atZone(KollavarshamCalendar.INDIAN_STANDARD_TIME).toLocalDate()
        val zone = KollavarshamCalendar.INDIAN_STANDARD_TIME
        val sunrise = SunriseSunset.sunrise(day, location.latitude, location.longitude, zone)
        val sunset = SunriseSunset.sunset(day, location.latitude, location.longitude, zone)
        require(sunrise != null && sunset != null) { "No sunrise or sunset on $day at $location" }
        val daylight = Duration.between(sunrise, sunset)
        val cutoff = sunrise.plus(daylight.multipliedBy(3).dividedBy(5))

        val margin = Duration.between(sankranti, cutoff)
        val firstDay = if (sankranti.isBefore(cutoff)) day else day.plusDays(1)
        return ComputedMonthStart(firstDay, sankranti, margin)
    }

    /** Mean date of the sankranti: Mesha around 14 April, then one rashi per 30.44 days. */
    private fun estimate(key: MonthKey, rashi: Int): LocalDate {
        // Months up to Meenam (ordinal 7) start in the Gregorian year ME + 824 or early the next;
        // Medam..Karkidakam (ordinals 8..11) in ME + 825.
        val meshaYear = key.year + KollavarshamCalendar.CHINGAM_GREGORIAN_OFFSET + if (key.month.ordinal >= 8) 1 else 0
        return LocalDate.of(meshaYear, 4, 14).plusDays(Math.round(rashi * 30.4375))
    }
}
