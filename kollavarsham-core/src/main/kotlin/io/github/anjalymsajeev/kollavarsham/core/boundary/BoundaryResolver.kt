package io.github.anjalymsajeev.kollavarsham.core.boundary

import io.github.anjalymsajeev.kollavarsham.core.BoundarySource
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.core.MonthBoundary
import io.github.anjalymsajeev.kollavarsham.core.MonthKey
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves month boundaries by layering overrides on a [MonthBoundarySource] and caches them
 * per Kollavarsham year.
 *
 * Precedence: user override, then verified override, then computation (ADR-0005).
 * Thread-safe: the cache holds immutable values, and concurrent computation of the same year
 * is harmless because the result is deterministic.
 */
internal class BoundaryResolver(
    private val source: MonthBoundarySource,
    verifiedOverrides: Map<MonthKey, LocalDate>,
    userOverrides: Map<MonthKey, LocalDate>,
) {
    private val overrides: Map<MonthKey, Pair<LocalDate, BoundarySource>> = buildMap {
        verifiedOverrides.forEach { (key, date) -> put(key, date to BoundarySource.VerifiedOverride) }
        userOverrides.forEach { (key, date) -> put(key, date to BoundarySource.UserOverride) }
    }

    private val cache = ConcurrentHashMap<Int, List<MonthBoundary>>()

    init {
        // Fail fast (at construction) if any override produces an impossible month length.
        overrides.keys.forEach { key ->
            checkLength(key.previous())
            checkLength(key)
        }
    }

    /** The twelve months of [year], Chingam first. */
    fun monthsOf(year: Int): List<MonthBoundary> = cache.getOrPut(year) { computeYear(year) }

    fun boundary(key: MonthKey): MonthBoundary = monthsOf(key.year)[key.month.ordinal]

    private fun firstDay(key: MonthKey): Pair<LocalDate, BoundarySource> =
        overrides[key] ?: (source.monthStart(key).firstDay to BoundarySource.Computed)

    private fun checkLength(key: MonthKey) {
        val length = firstDay(key.next()).first.toEpochDay() - firstDay(key).first.toEpochDay()
        require(length in MalayalamDate.MIN_DAYS_IN_MONTH..MalayalamDate.MAX_DAYS_IN_MONTH) {
            "Month-start overrides make ${key.month} ${key.year} $length days long; " +
                "months must be ${MalayalamDate.MIN_DAYS_IN_MONTH}..${MalayalamDate.MAX_DAYS_IN_MONTH} days"
        }
    }

    private fun computeYear(year: Int): List<MonthBoundary> {
        val keys = MalayalamMonth.entries.map { MonthKey(year, it) }
        val starts = (keys + MonthKey(year + 1, MalayalamMonth.Chingam)).map(::firstDay)
        return keys.mapIndexed { i, key ->
            val computed = source.monthStart(key)
            val (first, origin) = starts[i]
            MonthBoundary(
                year = year,
                month = key.month,
                firstDay = first,
                lastDay = starts[i + 1].first.minusDays(1),
                sankranti = computed.sankranti,
                cutoffMargin = computed.cutoffMargin,
                source = origin,
            )
        }
    }
}
