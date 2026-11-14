package io.github.anjalymsajeev.kollavarsham.core.boundary

import io.github.anjalymsajeev.kollavarsham.core.MonthKey
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * Supplies the first day of each month from a calendar rule.
 *
 * The astronomical implementation (Sprint 2) computes the sankranti and applies the Kerala
 * 3/5-of-daytime rule. Tests use table-driven fakes. Implementations must be deterministic and
 * thread-safe.
 */
internal fun interface MonthBoundarySource {
    fun monthStart(key: MonthKey): ComputedMonthStart
}

/** A computed month start, with the evidence needed by [io.github.anjalymsajeev.kollavarsham.core.MonthBoundary]. */
internal data class ComputedMonthStart(
    val firstDay: LocalDate,
    val sankranti: Instant,
    val cutoffMargin: Duration,
)
