package io.github.anjalymsajeev.kollavarsham.astronomy

import kotlin.math.PI

internal const val DEG_PER_RAD: Double = 180.0 / PI
internal const val ARCSEC_PER_DEG: Double = 3600.0
internal const val J2000: Double = 2451545.0
internal const val DAYS_PER_JULIAN_CENTURY: Double = 36525.0

internal fun Double.toRadians(): Double = this / DEG_PER_RAD

/** Normalises an angle to [0, 360). */
internal fun normalizeDegrees(degrees: Double): Double {
    val r = degrees % 360.0
    return if (r < 0) r + 360.0 else r
}

/** Normalises an angle difference to (-180, 180]. */
internal fun signedDegrees(degrees: Double): Double {
    val r = normalizeDegrees(degrees)
    return if (r > 180.0) r - 360.0 else r
}
