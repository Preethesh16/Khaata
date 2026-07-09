package com.khaata.app.agent

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * On-device speech-to-text for OFFLINE mode. Android's SpeechRecognizer with
 * offline preference — works in airplane mode when the Hindi/English language
 * packs are downloaded on the phone (Settings -> Google -> Voice -> Offline).
 */
class SpeechInputManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    fun startListening(
        languageTag: String = "hi-IN",
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            // Force on-device recognition only when there is no internet
            // (airplane-mode demo). Online, server-side recognition works
            // without any language pack installed.
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, !isOnline())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        val r = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                if (recognizer !== r) return  // stale session (superseded by fallback)
                val text = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (text.isNullOrBlank()) onError("Kuch samajh nahi aaya, dobara boliye")
                else onResult(text)
            }

            override fun onError(error: Int) {
                if (recognizer !== r) return  // stale session (superseded by fallback)
                Log.w("KhaataSTT", "recognizer error $error")
                // Offline language pack missing (newer Google app has no manual
                // download UI) — trigger the model download ourselves (API 33+).
                if (error == ERROR_LANG_UNAVAILABLE && Build.VERSION.SDK_INT >= 33) {
                    runCatching { r.triggerModelDownload(intent) }
                        .onSuccess { Log.i("KhaataSTT", "triggered $languageTag model download") }
                        .onFailure { Log.w("KhaataSTT", "model download trigger failed", it) }
                    // While the pack downloads, fall back to a locale whose pack is
                    // already on the phone (en-IN handles Hinglish fine).
                    if (languageTag != FALLBACK_LANG) {
                        Log.i("KhaataSTT", "falling back to $FALLBACK_LANG recognition")
                        startListening(FALLBACK_LANG, onResult, onError)
                    } else {
                        onError("Voice model download ho raha hai — wifi pe 1-2 min ruko, phir dobara boliye")
                    }
                    return
                }
                onError(
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Kuch sunai nahi diya, dobara boliye"
                        else -> "Mic error ($error), phir se try karo"
                    }
                )
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        r.startListening(intent)
    }

    fun stop() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        // SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE (API 33 constant, value 13)
        private const val ERROR_LANG_UNAVAILABLE = 13
        // en-IN understands Hinglish ("do kilo cheeni") well enough for the parser
        private const val FALLBACK_LANG = "en-IN"
    }
}
