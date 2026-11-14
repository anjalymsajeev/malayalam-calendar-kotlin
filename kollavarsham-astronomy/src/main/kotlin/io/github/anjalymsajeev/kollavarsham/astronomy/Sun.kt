package io.github.anjalymsajeev.kollavarsham.astronomy

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Apparent geocentric position of the Sun, referred to the true equinox of date. */
@KollavarshamAstronomyApi
public data class SolarPosition(
    /** Apparent tropical ecliptic longitude, degrees [0, 360). */
    val longitude: Double,
    /** Ecliptic latitude, degrees (always below about 0.0003°). */
    val latitude: Double,
    /** Right ascension, degrees [0, 360). */
    val rightAscension: Double,
    /** Declination, degrees. */
    val declination: Double,
    /** Nutation in longitude, degrees. */
    val nutationInLongitude: Double,
)

/**
 * The Sun's apparent position from the VSOP87D theory with the FK5 correction, nutation and
 * aberration (J. Meeus, Astronomical Algorithms, 2nd ed., ch. 22, 25, 32). Accuracy of the
 * longitude is about 1 arcsecond, which is about 25 seconds of time at a sankranti.
 */
@KollavarshamAstronomyApi
public object Sun {

    /** Apparent position at the Julian Ephemeris Day [jde] (Terrestrial Time). */
    public fun position(jde: Double): SolarPosition {
        val tau = (jde - J2000) / 365250.0
        val t = tau * 10.0

        val l = series(VSOP87D_EARTH_L, tau)
        val b = series(VSOP87D_EARTH_B, tau)
        val r = series(VSOP87D_EARTH_R, tau)

        // Heliocentric Earth -> geocentric Sun, then the FK5 correction (Meeus 32.3).
        var theta = normalizeDegrees(l * DEG_PER_RAD + 180.0)
        var beta = -b * DEG_PER_RAD
        val lambdaPrime = (theta - 1.397 * t - 0.00031 * t * t).toRadians()
        theta += -0.09033 / ARCSEC_PER_DEG
        beta += 0.03916 * (cos(lambdaPrime) - sin(lambdaPrime)) / ARCSEC_PER_DEG

        val nutation = Nutation.at(t)
        val aberration = -20.4898 / ARCSEC_PER_DEG / r
        val lambda = normalizeDegrees(theta + nutation.longitude + aberration)

        val eps = nutation.trueObliquity.toRadians()
        val lam = lambda.toRadians()
        val bet = beta.toRadians()
        val ra = atan2(sin(lam) * cos(eps) - kotlin.math.tan(bet) * sin(eps), cos(lam))
        val dec = asin(sin(bet) * cos(eps) + cos(bet) * sin(eps) * sin(lam))
        return SolarPosition(lambda, beta, normalizeDegrees(ra * DEG_PER_RAD), dec * DEG_PER_RAD, nutation.longitude)
    }

    private fun series(powers: Array<DoubleArray>, tau: Double): Double {
        var result = 0.0
        var tauPower = 1.0
        for (terms in powers) {
            var sum = 0.0
            var i = 0
            while (i < terms.size) {
                sum += terms[i] * cos(terms[i + 1] + terms[i + 2] * tau)
                i += 3
            }
            result += sum * tauPower
            tauPower *= tau
        }
        return result
    }
}

/**
 * Nutation and obliquity using Meeus's abridged series (ch. 22): nutation in longitude to
 * 0.5", in obliquity to 0.1".
 */
internal data class Nutation(val longitude: Double, val obliquity: Double, val meanObliquity: Double) {
    val trueObliquity: Double get() = meanObliquity + obliquity

    companion object {
        fun at(t: Double): Nutation {
            val omega = (125.04452 - 1934.136261 * t + 0.0020708 * t * t + t * t * t / 450000).toRadians()
            val l = (280.4665 + 36000.7698 * t).toRadians()
            val lMoon = (218.3165 + 481267.8813 * t).toRadians()
            val dPsi = -17.20 * sin(omega) - 1.32 * sin(2 * l) - 0.23 * sin(2 * lMoon) + 0.21 * sin(2 * omega)
            val dEps = 9.20 * cos(omega) + 0.57 * cos(2 * l) + 0.10 * cos(2 * lMoon) - 0.09 * cos(2 * omega)
            val eps0 = 23.0 + 26.0 / 60 + 21.448 / 3600 -
                (46.8150 * t + 0.00059 * t * t - 0.001813 * t * t * t) / ARCSEC_PER_DEG
            return Nutation(dPsi / ARCSEC_PER_DEG, dEps / ARCSEC_PER_DEG, eps0)
        }
    }
}
