package io.github.anjalymsajeev.kollavarsham.locale

/**
 * How Malayalam names are written in Latin script.
 */
public enum class Transliteration {
    /** Familiar ASCII spellings used in Kerala, for example "Chingam", "Vrischikam", "Njayar". */
    COMMON,

    /** ISO 15919 romanisation, for example "ciṅṅaṁ", "vr̥ścikaṁ", "ñāyar". */
    ISO_15919,
}

/**
 * Romanises Malayalam script following ISO 15919.
 *
 * Covered: independent vowels, consonants with the inherent vowel, vowel signs, virama, chillu
 * letters, anusvara, visarga and Malayalam digits (converted to Western digits). Characters
 * outside the Malayalam block pass through unchanged; ZWJ and ZWNJ are dropped.
 *
 * Simplifications, documented rather than hidden: the dental and alveolar na (both written ന)
 * are not distinguished and are rendered "n"; the archaic letters are not supported.
 *
 * ```kotlin
 * MalayalamTransliterator.toIso15919("ചിങ്ങം")      // "ciṅṅaṁ"
 * MalayalamTransliterator.toIso15919("കർക്കടകം")    // "karkkaṭakaṁ"
 * MalayalamTransliterator.toIso15919("കുംഭം")        // "kuṁbhaṁ" (anusvara is always ṁ)
 * ```
 */
public object MalayalamTransliterator {
    private val vowels =
        mapOf(
            'അ' to "a",
            'ആ' to "ā",
            'ഇ' to "i",
            'ഈ' to "ī",
            'ഉ' to "u",
            'ഊ' to "ū",
            'ഋ' to "r̥",
            'എ' to "e",
            'ഏ' to "ē",
            'ഐ' to "ai",
            'ഒ' to "o",
            'ഓ' to "ō",
            'ഔ' to "au",
        )

    private val consonants =
        mapOf(
            'ക' to "k",
            'ഖ' to "kh",
            'ഗ' to "g",
            'ഘ' to "gh",
            'ങ' to "ṅ",
            'ച' to "c",
            'ഛ' to "ch",
            'ജ' to "j",
            'ഝ' to "jh",
            'ഞ' to "ñ",
            'ട' to "ṭ",
            'ഠ' to "ṭh",
            'ഡ' to "ḍ",
            'ഢ' to "ḍh",
            'ണ' to "ṇ",
            'ത' to "t",
            'ഥ' to "th",
            'ദ' to "d",
            'ധ' to "dh",
            'ന' to "n",
            'പ' to "p",
            'ഫ' to "ph",
            'ബ' to "b",
            'ഭ' to "bh",
            'മ' to "m",
            'യ' to "y",
            'ര' to "r",
            'ല' to "l",
            'വ' to "v",
            'ശ' to "ś",
            'ഷ' to "ṣ",
            'സ' to "s",
            'ഹ' to "h",
            'ള' to "ḷ",
            'ഴ' to "ḻ",
            'റ' to "ṟ",
        )

    private val vowelSigns =
        mapOf(
            'ാ' to "ā",
            'ി' to "i",
            'ീ' to "ī",
            'ു' to "u",
            'ൂ' to "ū",
            'ൃ' to "r̥",
            'െ' to "e",
            'േ' to "ē",
            'ൈ' to "ai",
            'ൊ' to "o",
            'ോ' to "ō",
            'ൌ' to "au",
            'ൗ' to "au",
        )

    private val chillus = mapOf('ൺ' to "ṇ", 'ൻ' to "n", 'ർ' to "r", 'ൽ' to "l", 'ൾ' to "ḷ", 'ൿ' to "k")

    private const val VIRAMA = '്'
    private const val ANUSVARA = 'ം'
    private const val VISARGA = 'ഃ'
    private const val ZWJ = '‍'
    private const val ZWNJ = '‌'

    /** [text] romanised according to ISO 15919. */
    public fun toIso15919(text: String): String =
        buildString {
            var pendingInherentA = false

            fun flush() {
                if (pendingInherentA) append('a')
                pendingInherentA = false
            }
            for (ch in text) {
                when {
                    ch == ZWJ || ch == ZWNJ -> Unit
                    ch in consonants -> {
                        flush()
                        append(consonants.getValue(ch))
                        pendingInherentA = true
                    }
                    ch in vowelSigns -> {
                        append(vowelSigns.getValue(ch))
                        pendingInherentA = false
                    }
                    ch == VIRAMA -> pendingInherentA = false
                    else -> {
                        flush()
                        append(
                            when (ch) {
                                in vowels -> vowels.getValue(ch)
                                in chillus -> chillus.getValue(ch)
                                ANUSVARA -> "ṁ"
                                VISARGA -> "ḥ"
                                else -> MalayalamNumerals.toWesternDigits(ch.toString())
                            },
                        )
                    }
                }
            }
            flush()
        }
}
