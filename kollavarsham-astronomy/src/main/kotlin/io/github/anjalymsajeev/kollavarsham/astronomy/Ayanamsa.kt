package io.github.anjalymsajeev.kollavarsham.astronomy

/**
 * Lahiri (Chitrapaksha) ayanamsa conventions. Both are implemented so that the choice can be
 * made from almanac evidence (CALENDAR_ALGORITHM.md, Q1).
 */
@KollavarshamAstronomyApi
public enum class Ayanamsa {
    /**
     * 23°15′00.658″ at 1956-03-21 00:00 TT (Indian Calendar Reform Committee), carried forward by
     * IAU 2006 general precession, plus nutation in longitude (a "true" ayanamsa).
     */
    LahiriTrue,

    /**
     * 23.863092° at J2000.0 plus IAU 2006 precession, no nutation term: the convention the Dart
     * `kollavarsham` package calibrated to drikpanchang.com sankranti times.
     */
    LahiriDrikCalibrated,
    ;

    /** Ayanamsa in degrees at the Julian Ephemeris Day [jde], given the Sun's [nutationInLongitude] in degrees. */
    public fun degrees(jde: Double, nutationInLongitude: Double): Double {
        val t = (jde - J2000) / DAYS_PER_JULIAN_CENTURY
        return when (this) {
            LahiriTrue -> 23.250182778 + (precession(t) - precession(T_1956)) / ARCSEC_PER_DEG + nutationInLongitude
            LahiriDrikCalibrated -> 23.863092 + precession(t) / ARCSEC_PER_DEG
        }
    }

    private companion object {
        /** 1956-03-21 00:00 TT in Julian centuries from J2000. */
        const val T_1956: Double = (2435553.5 - J2000) / DAYS_PER_JULIAN_CENTURY

        /** IAU 2006 general precession in longitude, arcseconds. */
        fun precession(t: Double): Double = 5028.796195 * t + 1.1054348 * t * t
    }
}
