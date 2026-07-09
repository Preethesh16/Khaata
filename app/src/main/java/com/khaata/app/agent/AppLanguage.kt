package com.khaata.app.agent

import java.util.Locale

/** Languages the shopkeeper can bill in. Drives STT recognition + TTS voice. */
enum class AppLanguage(
    val label: String,          // shown on the language chip
    val sttTag: String,         // SpeechRecognizer language
    val ttsLocale: Locale
) {
    HINDI("हिंदी", "hi-IN", Locale("hi", "IN")),
    KANNADA("ಕನ್ನಡ", "kn-IN", Locale("kn", "IN")),
    ENGLISH("EN", "en-IN", Locale("en", "IN"));

    fun next(): AppLanguage = entries[(ordinal + 1) % entries.size]
}
