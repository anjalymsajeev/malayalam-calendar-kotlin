package io.github.anjalymsajeev.kollavarsham.locale

import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.core.MalayalamWeekday
import java.text.Normalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NamesTest {
    @Test fun `Malayalam month names`() {
        assertEquals(
            listOf("ചിങ്ങം", "കന്നി", "തുലാം", "വൃശ്ചികം", "ധനു", "മകരം", "കുംഭം", "മീനം", "മേടം", "ഇടവം", "മിഥുനം", "കർക്കടകം"),
            MalayalamMonth.entries.map { it.displayName(CalendarLocale.MALAYALAM) },
        )
    }

    @Test fun `Malayalam weekday names, Sunday first`() {
        assertEquals(
            listOf("ഞായർ", "തിങ്കൾ", "ചൊവ്വ", "ബുധൻ", "വ്യാഴം", "വെള്ളി", "ശനി"),
            MalayalamWeekday.entries.map { it.displayName(CalendarLocale.MALAYALAM) },
        )
    }

    @Test fun `English names are the transliterated identifiers`() {
        assertEquals("Vrischikam", MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH))
        assertEquals("Njayar", MalayalamWeekday.Njayar.displayName(CalendarLocale.ENGLISH))
    }

    @Test fun `Malayalam names are NFC, Malayalam-script only, and use atomic chillu letters`() {
        val names = MalayalamMonth.entries.map { it.displayName(CalendarLocale.MALAYALAM) } +
            MalayalamWeekday.entries.map { it.displayName(CalendarLocale.MALAYALAM) }
        names.forEach { name ->
            assertTrue(Normalizer.isNormalized(name, Normalizer.Form.NFC), name)
            assertTrue(name.all { it in 'ഀ'..'ൿ' }, "non-Malayalam character in $name")
            // Old-style chillu (consonant + virama + ZWJ) must not be used.
            assertTrue('‍' !in name, "ZWJ in $name")
        }
    }
}
