package io.github.anjalymsajeev.kollavarsham.astronomy

import java.time.Instant

/**
 * Time-scale helpers. [Instant] values are treated as Universal Time: UTC differs from UT1 by
 * under 0.9 s, which is negligible next to the minute-level effects that matter for month starts.
 */
@KollavarshamAstronomyApi
public object AstroTime {
    private const val UNIX_EPOCH_JD = 2440587.5
    private const val SECONDS_PER_DAY = 86400.0

    /** Julian Day (UT) of [instant]. */
    public fun julianDayUt(instant: Instant): Double =
        UNIX_EPOCH_JD + (instant.epochSecond + instant.nano / 1e9) / SECONDS_PER_DAY

    /** Converts a Julian Day (UT) back to an [Instant], rounded to the millisecond. */
    public fun instantOf(julianDayUt: Double): Instant =
        Instant.ofEpochMilli(Math.round((julianDayUt - UNIX_EPOCH_JD) * SECONDS_PER_DAY * 1000.0))

    /** Julian Ephemeris Day (Terrestrial Time) of the UT Julian Day [julianDayUt]. */
    public fun julianEphemerisDay(julianDayUt: Double): Double =
        julianDayUt + DeltaT.seconds(decimalYear(julianDayUt)) / SECONDS_PER_DAY

    internal fun decimalYear(julianDay: Double): Double = 2000.0 + (julianDay - J2000) / 365.25
}

/**
 * ΔT = TT − UT in seconds, from the Espenak–Meeus polynomial fits (NASA Five Millennium Canon of
 * Solar Eclipses, 2006) for 1860–2150. Within 1900–2025 the fits agree with observed ΔT to about
 * 2 seconds; later values are predictions whose uncertainty grows to roughly a minute by 2100.
 */
@KollavarshamAstronomyApi
public object DeltaT {

    /** Range of decimal years covered by the fits. */
    public val supportedYears: ClosedFloatingPointRange<Double> = 1860.0..2150.0

    /**
     * ΔT in seconds at [decimalYear].
     *
     * @throws IllegalArgumentException outside [supportedYears].
     */
    public fun seconds(decimalYear: Double): Double {
        val y = decimalYear
        require(y in supportedYears) { "ΔT is only defined for $supportedYears, was $y" }
        return when {
            y < 1900 -> {
                val t = y - 1860
                7.62 + 0.5737 * t - 0.251754 * t * t + 0.01680668 * t * t * t -
                    0.0004473624 * t * t * t * t + t * t * t * t * t / 233174
            }
            y < 1920 -> {
                val t = y - 1900
                -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t - 0.000197 * t * t * t * t
            }
            y < 1941 -> {
                val t = y - 1920
                21.20 + 0.84493 * t - 0.076100 * t * t + 0.0020936 * t * t * t
            }
            y < 1961 -> {
                val t = y - 1950
                29.07 + 0.407 * t - t * t / 233 + t * t * t / 2547
            }
            y < 1986 -> {
                val t = y - 1975
                45.45 + 1.067 * t - t * t / 260 - t * t * t / 718
            }
            y < 2005 -> {
                val t = y - 2000
                63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t * t * t +
                    0.000651814 * t * t * t * t + 0.00002373599 * t * t * t * t * t
            }
            y < 2050 -> {
                val t = y - 2000
                62.92 + 0.32217 * t + 0.005589 * t * t
            }
            else -> {
                val u = (y - 1820) / 100
                -20 + 32 * u * u - 0.5628 * (2150 - y)
            }
        }
    }
}
