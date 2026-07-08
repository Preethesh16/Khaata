package com.khaata.app.util

/**
 * Parses quantities from Hindi / Kannada / English speech.
 * "do kilo cheeni" -> 2.0, "adha kilo" -> 0.5, "3 packet" -> 3.0
 */
object QuantityParser {

    val NUMBER_WORDS = mapOf(
        // Hindi
        "ek" to 1.0, "do" to 2.0, "teen" to 3.0, "char" to 4.0, "chaar" to 4.0,
        "paanch" to 5.0, "panch" to 5.0, "che" to 6.0, "chhe" to 6.0, "saat" to 7.0,
        "aath" to 8.0, "nau" to 9.0, "das" to 10.0, "adha" to 0.5, "aadha" to 0.5,
        "paav" to 0.25, "pav" to 0.25, "dedh" to 1.5, "dhai" to 2.5, "adhai" to 2.5,
        // Devanagari
        "एक" to 1.0, "दो" to 2.0, "तीन" to 3.0, "चार" to 4.0, "पांच" to 5.0, "पाँच" to 5.0,
        "छह" to 6.0, "सात" to 7.0, "आठ" to 8.0, "नौ" to 9.0, "दस" to 10.0,
        "आधा" to 0.5, "पाव" to 0.25, "डेढ़" to 1.5, "ढाई" to 2.5,
        // Kannada
        "ondu" to 1.0, "eradu" to 2.0, "mooru" to 3.0, "nalku" to 4.0, "aidu" to 5.0,
        "ಒಂದು" to 1.0, "ಎರಡು" to 2.0, "ಮೂರು" to 3.0, "ನಾಲ್ಕು" to 4.0, "ಐದು" to 5.0,
        "ardha" to 0.5, "ಅರ್ಧ" to 0.5,
        // English
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
        "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
        "half" to 0.5, "quarter" to 0.25, "a" to 1.0, "an" to 1.0
    )

    /** Extracts the first quantity found in the phrase; defaults to 1.0. */
    fun parse(phrase: String): Double {
        for (token in phrase.lowercase().split(Regex("[\\s,]+"))) {
            token.toDoubleOrNull()?.let { if (it > 0) return it }
            NUMBER_WORDS[token]?.let { return it }
        }
        return 1.0
    }

    /** Removes quantity + unit words so the remainder is the item name. */
    fun stripQuantityWords(phrase: String): String {
        val unitWords = setOf(
            "kilo", "kg", "kilogram", "gram", "packet", "pkt", "packets", "piece", "pieces",
            "litre", "liter", "ltr", "dozen", "darjan", "bottle", "box",
            "किलो", "पैकेट", "लीटर", "दर्जन", "ಕಿಲೋ", "ಪ್ಯಾಕೆಟ್", "ಲೀಟರ್",
            "de", "do", "dena", "dijiye", "chahiye", "kodi", "beku"
        )
        return phrase.lowercase().split(Regex("[\\s,]+"))
            .filter { it.isNotBlank() }
            .filter { it.toDoubleOrNull() == null }
            .filterIndexed { idx, t -> !(NUMBER_WORDS.containsKey(t) && idx <= 1) }
            .filter { it !in unitWords }
            .joinToString(" ")
            .trim()
    }
}
