package io.github.anjalymsajeev.kollavarsham.astronomy

/**
 * Marks astronomy declarations whose API may change before 1.0.0.
 *
 * The astronomy module is shared by `kollavarsham-core` (sankranti and sunrise for month
 * boundaries) and, from Sprint 4, `kollavarsham-panchangam`. Callers outside the SDK must
 * opt in explicitly:
 *
 * ```kotlin
 * @OptIn(KollavarshamAstronomyApi::class)
 * fun example() { /* use astronomy APIs */ }
 * ```
 */
@RequiresOptIn(
    message = "Astronomy APIs are not yet stable and may change before 1.0.0.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class KollavarshamAstronomyApi
