package io.github.anjalymsajeev.kollavarsham.astronomy

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Geocentric position of the Moon referred to the true equinox of date. */
@KollavarshamAstronomyApi
public data class LunarPosition(
    /** Apparent tropical ecliptic longitude (nutation included), degrees [0, 360). */
    val longitude: Double,
    /** Ecliptic latitude, degrees. */
    val latitude: Double,
    /** Distance from the Earth's centre, km. */
    val distanceKm: Double,
    /** Nutation in longitude used, degrees. */
    val nutationInLongitude: Double,
)

/**
 * The Moon's position from the abridged ELP-2000/82 theory (J. Meeus, Astronomical Algorithms,
 * 2nd ed., ch. 47): about 10″ in longitude, roughly 20 seconds of time for a nakshatra
 * boundary and 25 seconds for a tithi boundary.
 */
@KollavarshamAstronomyApi
public object Moon {
    /** Position at the Julian Ephemeris Day [jde] (Terrestrial Time). */
    public fun position(jde: Double): LunarPosition {
        val t = (jde - J2000) / DAYS_PER_JULIAN_CENTURY
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        val lp = normalizeDegrees(218.3164477 + 481267.88123421 * t - 0.0015786 * t2 + t3 / 538841 - t4 / 65194000)
        val d = normalizeDegrees(297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868 - t4 / 113065000)
        val m = normalizeDegrees(357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000)
        val mp = normalizeDegrees(134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699 - t4 / 14712000)
        val f = normalizeDegrees(93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000 + t4 / 863310000)
        val a1 = normalizeDegrees(119.75 + 131.849 * t)
        val a2 = normalizeDegrees(53.09 + 479264.290 * t)
        val a3 = normalizeDegrees(313.45 + 481266.484 * t)
        val e = 1 - 0.002516 * t - 0.0000074 * t2

        var sumL = 0.0
        var sumR = 0.0
        var i = 0
        val ta = MOON_LONGITUDE_DISTANCE_TERMS
        while (i < ta.size) {
            val arg = (ta[i] * d + ta[i + 1] * m + ta[i + 2] * mp + ta[i + 3] * f).toRadians()
            val ecc = eccentricityFactor(ta[i + 1], e)
            sumL += ta[i + 4] * ecc * sin(arg)
            sumR += ta[i + 5] * ecc * cos(arg)
            i += 6
        }
        var sumB = 0.0
        i = 0
        val tb = MOON_LATITUDE_TERMS
        while (i < tb.size) {
            val arg = (tb[i] * d + tb[i + 1] * m + tb[i + 2] * mp + tb[i + 3] * f).toRadians()
            sumB += tb[i + 4] * eccentricityFactor(tb[i + 1], e) * sin(arg)
            i += 5
        }

        // Additive terms: Venus (A1), Jupiter (A2) and the flattening of the Earth (L' - F).
        sumL += 3958 * sin(a1.toRadians()) + 1962 * sin((lp - f).toRadians()) + 318 * sin(a2.toRadians())
        sumB += -2235 * sin(lp.toRadians()) + 382 * sin(a3.toRadians()) + 175 * sin((a1 - f).toRadians()) +
            175 * sin((a1 + f).toRadians()) + 127 * sin((lp - mp).toRadians()) - 115 * sin((lp + mp).toRadians())

        val nutation = Nutation.at(t).longitude
        return LunarPosition(
            longitude = normalizeDegrees(lp + sumL / 1e6 + nutation),
            latitude = sumB / 1e6,
            distanceKm = 385000.56 + sumR / 1000,
            nutationInLongitude = nutation,
        )
    }

    private fun eccentricityFactor(
        mMultiple: Int,
        e: Double,
    ): Double =
        when (abs(mMultiple)) {
            1 -> e
            2 -> e * e
            else -> 1.0
        }
}
