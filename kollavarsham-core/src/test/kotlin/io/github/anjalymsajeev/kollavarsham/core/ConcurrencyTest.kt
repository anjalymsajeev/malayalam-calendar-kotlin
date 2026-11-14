package io.github.anjalymsajeev.kollavarsham.core

import java.time.LocalDate
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrencyTest {
    @Test fun `concurrent conversions give the same results as sequential ones`() {
        val days = List(20_000) { LocalDate.of(1900, 1, 1).plusDays(Random(it).nextLong(73_000)) }
        val expected = SyntheticBoundarySource.calendar().let { c -> days.map(c::fromGregorian) }

        val shared = SyntheticBoundarySource.calendar()
        val pool = Executors.newFixedThreadPool(32)
        try {
            val results = pool.invokeAll(
                (0 until 32).map { t ->
                    Callable { days.indices.filter { it % 32 == t }.map { it to shared.fromGregorian(days[it]) } }
                },
            ).flatMap { it.get() }
            results.forEach { (i, date) -> assertEquals(expected[i], date, "index $i") }
        } finally {
            pool.shutdown()
            pool.awaitTermination(10, TimeUnit.SECONDS)
        }
    }
}
