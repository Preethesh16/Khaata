package com.khaata.app.agent

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/** Offline voice confirmations via Android's built-in TTS (Hindi). */
class TtsManager(context: Context) {

    private var ready = false
    private val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ready = true
        } else {
            Log.e("KhaataTTS", "TTS init failed: $status")
        }
    }

    init {
        // language set lazily after init callback; safe to call again on speak
    }

    fun speak(text: String, locale: Locale = Locale("hi", "IN")) {
        if (!ready) return
        tts.language = locale
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, text.hashCode().toString())
    }

    fun stop() = tts.stop()

    fun shutdown() = tts.shutdown()
}
