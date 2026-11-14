package io.github.anjalymsajeev.kollavarsham.locale

import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.core.MalayalamWeekday
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MalayalamNumeralsTest {
    @Test fun `formats with Malayalam digits`() {
        assertEquals("൦൧൨൩൪൫൬൭൮൯", MalayalamNumerals.toMalayalamDigits("0123456789"))
        assertEquals("൧൨൦൨", MalayalamNumerals.format(1202))
        assertEquals("-൧൫", MalayalamNumerals.format(-15L))
        assertEquals("൨൮/൦൩, abc", MalayalamNumerals.toMalayalamDigits("28/03, abc"))
    }

    @Test fun `round-trips and parses`() {
        for (n in listOf(0L, 7L, 1202L, -42L, Long.MAX_VALUE)) {
            assertEquals(n, MalayalamNumerals.parse(MalayalamNumerals.format(n)))
        }
        assertEquals(1202L, MalayalamNumerals.parse(" ൧2൦2 "))
        assertEquals("28/03", MalayalamNumerals.toWesternDigits("൨൮/൦൩"))
        assertFailsWith<NumberFormatException> { MalayalamNumerals.parse("൧൨a") }
    }
}

class MalayalamTransliteratorTest {
    @Test fun `ISO 15919 month names`() {
        assertEquals(
            listOf(
                "ciṅṅaṁ",
                "kanni",
                "tulāṁ",
                "vr̥ścikaṁ",
                "dhanu",
                "makaraṁ",
                "kuṁbhaṁ",
                "mīnaṁ",
                "mēṭaṁ",
                "iṭavaṁ",
                "mithunaṁ",
                "karkkaṭakaṁ",
            ),
            MalayalamMonth.entries.map { MalayalamTransliterator.toIso15919(it.displayName(CalendarLocale.MALAYALAM)) },
        )
    }

    @Test fun `ISO 15919 weekday names`() {
        assertEquals(
            listOf("ñāyar", "tiṅkaḷ", "covva", "budhan", "vyāḻaṁ", "veḷḷi", "śani"),
            MalayalamWeekday.entries.map { MalayalamTransliterator.toIso15919(it.displayName(CalendarLocale.MALAYALAM)) },
        )
    }

    @Test fun `vowels, virama, visarga, digits and passthrough`() {
        assertEquals("kēraḷaṁ", MalayalamTransliterator.toIso15919("കേരളം"))
        assertEquals("malayāḷaṁ", MalayalamTransliterator.toIso15919("മലയാളം"))
        assertEquals("kollavarṣaṁ 1202", MalayalamTransliterator.toIso15919("കൊല്ലവർഷം ൧൨൦൨"))
        assertEquals("duḥkhaṁ", MalayalamTransliterator.toIso15919("ദുഃഖം"))
        assertEquals("aikyaṁ", MalayalamTransliterator.toIso15919("ഐക്യം"))
        assertEquals("ṟ", MalayalamTransliterator.toIso15919("റ്"))
        assertEquals("Kerala (kēraḷaṁ)", MalayalamTransliterator.toIso15919("Kerala (കേരളം)"))
        // Old-style chillu with ZWJ gives the same result as the atomic chillu, apart from the letter used.
        assertEquals("avan", MalayalamTransliterator.toIso15919("അവന്‍"))
        assertEquals("", MalayalamTransliterator.toIso15919(""))
    }

    @Test fun `ISO display names are capitalised`() {
        assertEquals("Ciṅṅaṁ", MalayalamMonth.Chingam.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919))
        assertEquals("Ñāyar", MalayalamWeekday.Njayar.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919))
        // Transliteration does not affect Malayalam output.
        assertEquals("ചിങ്ങം", MalayalamMonth.Chingam.displayName(CalendarLocale.MALAYALAM, Transliteration.ISO_15919))
    }
}
