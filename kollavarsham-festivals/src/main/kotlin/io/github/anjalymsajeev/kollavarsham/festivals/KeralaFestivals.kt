package io.github.anjalymsajeev.kollavarsham.festivals

import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.DaysFrom
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.DaysFromEaster
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.FirstSunriseAfterSankranti
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.FirstTithiOnOrAfter
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.NakshatraInMonth
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.OnGregorianDate
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.OnMalayalamDate
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.TithiInLunarMonth
import io.github.anjalymsajeev.kollavarsham.festivals.FestivalRule.TithiInMonth
import io.github.anjalymsajeev.kollavarsham.festivals.ObservanceCategory.CHRISTIAN
import io.github.anjalymsajeev.kollavarsham.festivals.ObservanceCategory.CIVIC
import io.github.anjalymsajeev.kollavarsham.festivals.ObservanceCategory.HINDU
import io.github.anjalymsajeev.kollavarsham.festivals.ObservanceCategory.TEMPLE
import io.github.anjalymsajeev.kollavarsham.panchangam.Nakshatra
import io.github.anjalymsajeev.kollavarsham.panchangam.Tithi
import java.time.Month

/**
 * The built-in Kerala festival definitions. Each rule is validated against cited dates
 * (`kollavarsham-festivals/src/test/resources/reference/festival-dates.csv`; VALIDATION_STRATEGY.md).
 *
 * Left out on purpose until a rule can be validated: Ashtami Rohini (the rule is not simple),
 * Dhanu Thiruvathira (sources disagree), Thaipooyam (no Kerala source), and the Islamic festivals,
 * whose dates follow moon sighting. Apps can add any of these from dated lists with
 * [DatedObservances].
 */
public object KeralaFestivals {
    public val MALAYALAM_NEW_YEAR: Festival =
        Festival("malayalam-new-year", "Malayalam New Year (Chingam 1)", "ചിങ്ങം 1", HINDU, OnMalayalamDate(MalayalamMonth.Chingam, 1))

    /** Thiruvonam at sunrise in Chingam; when it occurs twice, Kerala keeps the later day. */
    public val ONAM: Festival =
        Festival(
            "onam",
            "Onam (Thiruvonam)",
            "തിരുവോണം",
            HINDU,
            NakshatraInMonth(MalayalamMonth.Chingam, Nakshatra.Thiruvonam, occurrence = Occurrence.LAST),
        )

    /** Atham, ten days of celebration ending with Thiruvonam. */
    public val ATHAM: Festival = Festival("atham", "Atham", "അത്തം", HINDU, DaysFrom("onam", -9))

    public val VISHU: Festival = Festival("vishu", "Vishu", "വിഷു", HINDU, FirstSunriseAfterSankranti(MalayalamMonth.Medam))

    /** Amavasya in Karkidakam read late in the afternoon (4/5 of daylight), else at sunrise; the first if two. */
    public val KARKIDAKA_VAVU: Festival =
        Festival(
            "karkidaka-vavu",
            "Karkidaka Vavu",
            "കർക്കടക വാവ്",
            HINDU,
            TithiInMonth(
                MalayalamMonth.Karkidakam,
                Tithi.KrishnaAmavasya,
                listOf(ReadingTime.FractionOfDaylight(0.8), ReadingTime.Sunrise),
            ),
        )

    /** Krishna Chaturdashi at midnight, in the lunar month holding Kumbham's sankranti. */
    public val SIVARATRI: Festival =
        Festival(
            "sivaratri",
            "Maha Sivaratri",
            "ശിവരാത്രി",
            HINDU,
            TithiInLunarMonth(MalayalamMonth.Kumbham, Tithi.KrishnaChaturdashi, ReadingTime.Midnight),
        )

    /** Shukla Chaturthi at mid-day, in the lunar month holding Kanni's sankranti. */
    public val VINAYAKA_CHATHURTHI: Festival =
        Festival(
            "vinayaka-chathurthi",
            "Vinayaka Chathurthi",
            "വിനായക ചതുർത്ഥി",
            HINDU,
            TithiInLunarMonth(MalayalamMonth.Kanni, Tithi.ShuklaChaturthi, ReadingTime.FractionOfDaylight(0.5)),
        )

    /** Shukla Dashami at sunrise, in the lunar month holding Thulam's sankranti. */
    public val VIJAYADASAMI: Festival =
        Festival(
            "vijayadasami",
            "Vijayadasami",
            "വിജയദശമി",
            HINDU,
            TithiInLunarMonth(MalayalamMonth.Thulam, Tithi.ShuklaDashami),
        )

    public val MAHANAVAMI: Festival = Festival("mahanavami", "Mahanavami", "മഹാനവമി", HINDU, DaysFrom("vijayadasami", -1))

    public val DURGASHTAMI: Festival = Festival("durgashtami", "Durgashtami", "ദുർഗാഷ്ടമി", HINDU, DaysFrom("vijayadasami", -2))

    /** Krishna Chaturdashi at dawn (96 minutes before sunrise), in the lunar month holding Thulam's sankranti. */
    public val DEEPAVALI: Festival =
        Festival(
            "deepavali",
            "Deepavali",
            "ദീപാവലി",
            HINDU,
            TithiInLunarMonth(MalayalamMonth.Thulam, Tithi.KrishnaChaturdashi, ReadingTime.BeforeSunrise(96)),
        )

    /** Pooram at midnight in Medam; the later day if two. */
    public val THRISSUR_POORAM: Festival =
        Festival(
            "thrissur-pooram",
            "Thrissur Pooram",
            "തൃശ്ശൂർ പൂരം",
            TEMPLE,
            NakshatraInMonth(MalayalamMonth.Medam, Nakshatra.Pooram, ReadingTime.Midnight, Occurrence.LAST),
        )

    /** Pooram at sunset in Kumbham; the later day if two. */
    public val ATTUKAL_PONGALA: Festival =
        Festival(
            "attukal-pongala",
            "Attukal Pongala",
            "ആറ്റുകാൽ പൊങ്കാല",
            TEMPLE,
            NakshatraInMonth(MalayalamMonth.Kumbham, Nakshatra.Pooram, ReadingTime.Sunset, Occurrence.LAST),
        )

    /** The first Shukla Ekadashi at sunrise from Vrischikam 1. */
    public val GURUVAYUR_EKADASHI: Festival =
        Festival(
            "guruvayur-ekadashi",
            "Guruvayur Ekadashi",
            "ഗുരുവായൂർ ഏകാദശി",
            TEMPLE,
            FirstTithiOnOrAfter(MalayalamMonth.Vrischikam, 1, Tithi.ShuklaEkadashi),
        )

    /** Sabarimala Mandala season begins on Vrischikam 1. */
    public val MANDALA_KALAM_START: Festival =
        Festival(
            "mandala-kalam-start",
            "Mandala season begins",
            "മണ്ഡലകാലാരംഭം",
            TEMPLE,
            OnMalayalamDate(MalayalamMonth.Vrischikam, 1),
        )

    /** The 41st day of the Mandala season. */
    public val MANDALA_POOJA: Festival =
        Festival("mandala-pooja", "Mandala Pooja", "മണ്ഡലപൂജ", TEMPLE, DaysFrom("mandala-kalam-start", 40))

    public val MAKARAVILAKKU: Festival =
        Festival("makaravilakku", "Makaravilakku", "മകരവിളക്ക്", TEMPLE, OnMalayalamDate(MalayalamMonth.Makaram, 1))

    public val KERALA_PIRAVI: Festival =
        Festival("kerala-piravi", "Kerala Piravi", "കേരളപ്പിറവി", CIVIC, OnGregorianDate(Month.NOVEMBER, 1))
    public val REPUBLIC_DAY: Festival =
        Festival("republic-day", "Republic Day", "റിപ്പബ്ലിക് ദിനം", CIVIC, OnGregorianDate(Month.JANUARY, 26))
    public val INDEPENDENCE_DAY: Festival =
        Festival("independence-day", "Independence Day", "സ്വാതന്ത്ര്യ ദിനം", CIVIC, OnGregorianDate(Month.AUGUST, 15))
    public val GANDHI_JAYANTI: Festival =
        Festival("gandhi-jayanti", "Gandhi Jayanti", "ഗാന്ധി ജയന്തി", CIVIC, OnGregorianDate(Month.OCTOBER, 2))

    public val CHRISTMAS: Festival = Festival("christmas", "Christmas", "ക്രിസ്മസ്", CHRISTIAN, OnGregorianDate(Month.DECEMBER, 25))
    public val EASTER: Festival = Festival("easter", "Easter", "ഈസ്റ്റർ", CHRISTIAN, DaysFromEaster(0))
    public val GOOD_FRIDAY: Festival = Festival("good-friday", "Good Friday", "ദുഃഖ വെള്ളി", CHRISTIAN, DaysFromEaster(-2))
    public val MAUNDY_THURSDAY: Festival = Festival("maundy-thursday", "Maundy Thursday", "പെസഹ വ്യാഴം", CHRISTIAN, DaysFromEaster(-3))

    /** All built-in festivals. */
    public val all: List<Festival> =
        listOf(
            MALAYALAM_NEW_YEAR,
            ATHAM,
            ONAM,
            VISHU,
            KARKIDAKA_VAVU,
            SIVARATRI,
            VINAYAKA_CHATHURTHI,
            DURGASHTAMI,
            MAHANAVAMI,
            VIJAYADASAMI,
            DEEPAVALI,
            THRISSUR_POORAM,
            ATTUKAL_PONGALA,
            GURUVAYUR_EKADASHI,
            MANDALA_KALAM_START,
            MANDALA_POOJA,
            MAKARAVILAKKU,
            KERALA_PIRAVI,
            REPUBLIC_DAY,
            INDEPENDENCE_DAY,
            GANDHI_JAYANTI,
            CHRISTMAS,
            EASTER,
            GOOD_FRIDAY,
            MAUNDY_THURSDAY,
        )
}
