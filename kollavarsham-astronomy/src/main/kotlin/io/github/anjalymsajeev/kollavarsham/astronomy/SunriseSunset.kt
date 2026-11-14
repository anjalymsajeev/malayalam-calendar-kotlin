package io.github.anjalymsajeev.kollavarsham.astronomy

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sunrise and sunset: the Sun's upper limb on the horizon with standard refraction, i.e. a true
 * altitude of the centre of −0.8333° (Meeus ch. 15), solved iteratively with the apparent solar
 * position. Accuracy is limited by atmospheric refraction to about a minute.
 */
@KollavarshamAstronomyApi
public object SunriseSunset {
    private const val STANDARD_ALTITUDE = -0.8333
    private const val SIDEREAL_DEGREES_PER_DAY = 360.98564736629
    private const val MAX_ITERATIONS = 12

    /** Sunrise on the civil [date] in [zone] at [latitude], [longitude] (degrees, east positive), or `null` if the Sun does not rise. */
    public fun sunrise(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
    ): Instant? = event(date, latitude, longitude, zone, rising = true)

    /** Sunset on the civil [date] in [zone]; `null` if the Sun does not set. */
    public fun sunset(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
    ): Instant? = event(date, latitude, longitude, zone, rising = false)

    private fun event(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        rising: Boolean,
    ): Instant? {
        val localNoon = date.atTime(12, 0).atZone(zone).toInstant()
        var jd = AstroTime.julianDayUt(localNoon)
        val phi = latitude.toRadians()
        repeat(MAX_ITERATIONS) {
            val sun = Sun.position(AstroTime.julianEphemerisDay(jd))
            val dec = sun.declination.toRadians()
            val cosH0 = (sin(STANDARD_ALTITUDE.toRadians()) - sin(phi) * sin(dec)) / (cos(phi) * cos(dec))
            if (cosH0 < -1.0 || cosH0 > 1.0) return null
            val h0 = acos(cosH0) * DEG_PER_RAD
            val hourAngle = signedDegrees(greenwichSiderealTime(jd) + longitude - sun.rightAscension)
            val target = if (rising) -h0 else h0
            val correction = signedDegrees(hourAngle - target) / SIDEREAL_DEGREES_PER_DAY
            jd -= correction
            if (abs(correction) < 1e-7) return AstroTime.instantOf(jd)
        }
        return AstroTime.instantOf(jd)
    }

    /** Mean sidereal time at Greenwich in degrees (Meeus 12.4); nutation's ≤1 s effect is ignored. */
    private fun greenwichSiderealTime(jdUt: Double): Double {
        val t = (jdUt - J2000) / DAYS_PER_JULIAN_CENTURY
        return normalizeDegrees(
            280.46061837 + 360.98564736629 * (jdUt - J2000) + 0.000387933 * t * t - t * t * t / 38710000.0,
        )
    }
}
