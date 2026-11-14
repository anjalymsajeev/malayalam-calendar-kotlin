package io.github.anjalymsajeev.kollavarsham.astronomy

import java.time.Instant
import kotlin.math.abs

/** Sidereal solar longitude and sankranti (solar ingress) times. */
@KollavarshamAstronomyApi
public object Sankranti {
    private const val MEAN_DEGREES_PER_DAY = 0.98564736
    private const val TOLERANCE_DAYS = 1.0 / 86400.0 / 10.0 // 0.1 s
    private const val MAX_ITERATIONS = 20

    /** The Sun's nirayana (sidereal) longitude in degrees [0, 360) at [instant]. */
    public fun siderealLongitude(
        instant: Instant,
        ayanamsa: Ayanamsa,
    ): Double = siderealLongitudeAt(AstroTime.julianDayUt(instant), ayanamsa)

    /**
     * The moment the Sun's sidereal longitude reaches `rashi × 30°` nearest to [estimate]
     * (rashi 0 = Mesha … 11 = Meena). [estimate] must be within about ten days of the event.
     *
     * @throws IllegalArgumentException if [rashi] is not in 0..11.
     */
    public fun find(
        rashi: Int,
        estimate: Instant,
        ayanamsa: Ayanamsa,
    ): Instant {
        require(rashi in 0..11) { "rashi must be in 0..11, was $rashi" }
        val target = rashi * 30.0
        var jd = AstroTime.julianDayUt(estimate)
        repeat(MAX_ITERATIONS) {
            val step = signedDegrees(target - siderealLongitudeAt(jd, ayanamsa)) / MEAN_DEGREES_PER_DAY
            jd += step
            if (abs(step) < TOLERANCE_DAYS) return AstroTime.instantOf(jd)
        }
        error("Sankranti search for rashi $rashi near $estimate did not converge")
    }

    private fun siderealLongitudeAt(
        jdUt: Double,
        ayanamsa: Ayanamsa,
    ): Double {
        val jde = AstroTime.julianEphemerisDay(jdUt)
        val sun = Sun.position(jde)
        return normalizeDegrees(sun.longitude - ayanamsa.degrees(jde, sun.nutationInLongitude))
    }
}
