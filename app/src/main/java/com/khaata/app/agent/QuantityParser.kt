package com.khaata.app.agent

import java.util.Locale

object QuantityParser {

    private val numberWords = mapOf(
        // Hindi
        "ek" to 1.0, "do" to 2.0, "teen" to 3.0, "char" to 4.0, "chaar" to 4.0,
        "paanch" to 5.0, "panch" to 5.0, "che" to 6.0, "cheh" to 6.0, "saat" to 7.0,
        "aath" to 8.0, "nau" to 9.0, "das" to 10.0, "adha" to 0.5, "aadha" to 0.5,
        "paav" to 0.25, "pav" to 0.25, "dedh" to 1.5, "dhai" to 2.5, "dhaai" to 2.5,
        // Kannada
        "ondu" to 1.0, "eradu" to 2.0, "mooru" to 3.0, "nalku" to 4.0, "aidu" to 5.0,
        "aaru" to 6.0, "elu" to 7.0, "entu" to 8.0, "ombattu" to 9.0, "hattu" to 10.0,
        "ardha" to 0.5,
        // English
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
        "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
        "half" to 0.5, "quarter" to 0.25, "a" to 1.0, "an" to 1.0
    )

    private val digitRegex = Regex("""\d+(\.\d+)?""")

    /** Extracts the first quantity found in the phrase; defaults to 1.0. */
    fun parse(phrase: String): Double {
        val normalized = phrase.lowercase(Locale.ROOT)
        digitRegex.find(normalized)?.let { return it.value.toDouble() }
        normalized.split(Regex("""[\s,]+""")).forEach { word ->
            numberWords[word]?.let { return it }
        }
        return 1.0
    }

    /** Removes quantity words/digits and unit words from a phrase, leaving the item name. */
    fun stripQuantity(phrase: String): String {
        val unitWords = setOf(
            "kilo", "kg", "kilogram", "gram", "packet", "pkt", "packets", "piece",
            "pieces", "litre", "liter", "dozen", "darjan", "bottle", "ka", "ki", "ke"
        )
        return phrase.lowercase(Locale.ROOT)
            .replace(digitRegex, " ")
            .split(Regex("""[\s,]+"""))
            .filter { it.isNotBlank() && it !in numberWords.keys && it !in unitWords }
            .joinToString(" ")
            .trim()
    }
}
