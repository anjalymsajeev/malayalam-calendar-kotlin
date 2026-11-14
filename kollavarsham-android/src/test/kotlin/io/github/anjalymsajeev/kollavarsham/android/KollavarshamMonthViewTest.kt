package io.github.anjalymsajeev.kollavarsham.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class KollavarshamMonthViewTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsWeekdaysAndDatesAndReportsTaps() {
        var tapped: LocalDate? = null
        compose.setContent {
            MaterialTheme {
                KollavarshamMonthView(
                    grid = MonthGrid.ofGregorianMonth(YearMonth.of(2026, 11)),
                    selectedDate = LocalDate.of(2026, 11, 14),
                    onDateSelected = { tapped = it },
                    today = LocalDate.of(2026, 11, 14),
                )
            }
        }
        compose.onNodeWithText("ഞായർ").assertExists()
        compose.onNodeWithContentDescription("2026-11-14, 1202 തുലാം 28").assertIsSelected()
        compose.onNodeWithContentDescription("2026-11-17, 1202 വൃശ്ചികം 1").performClick()
        assertEquals(LocalDate.of(2026, 11, 17), tapped)
    }
}
