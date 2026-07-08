package com.khaata.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Offline voice confirmations using Android's built-in TTS (Hindi).
 * Works in airplane mode — no dependency, no network.
 */
class TtsManager(context: Context) {

    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            setLanguage(Locale("hi", "IN"))
        }
    }

    fun setLanguage(locale: Locale) {
        runCatching {
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.US
            }
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "khaata-${System.currentTimeMillis()}")
    }

    fun stop() = runCatching { tts.stop() }

    fun shutdown() = runCatching { tts.shutdown() }
}
