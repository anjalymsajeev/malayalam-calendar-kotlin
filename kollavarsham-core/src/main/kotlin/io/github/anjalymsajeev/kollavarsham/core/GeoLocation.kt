package io.github.anjalymsajeev.kollavarsham.core

/**
 * A point on Earth used as the reference location for sunrise and sunset.
 *
 * ```kotlin
 * val kozhikode = GeoLocation(latitude = 11.2588, longitude = 75.7804, name = "Kozhikode")
 * ```
 *
 * @property latitude degrees north, -90..90.
 * @property longitude degrees east, -180..180.
 * @property name optional human-readable name.
 */
public data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0) { "Latitude must be in -90..90, was $latitude" }
        require(longitude.isFinite() && longitude in -180.0..180.0) { "Longitude must be in -180..180, was $longitude" }
    }

    public companion object {
        /** Thiruvananthapuram, Kerala: the default reference location. */
        public val Thiruvananthapuram: GeoLocation = GeoLocation(8.5241, 76.9366, "Thiruvananthapuram")
    }
}
