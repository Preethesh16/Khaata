package com.khaata.app.agent

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.khaata.app.data.CatalogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * OFFLINE mode. Gemma on-device via MediaPipe LLM Inference (LiteRT).
 *
 * Pipeline: on-device SpeechRecognizer gives us the utterance text ->
 * Gemma parses it into structured actions -> same AgentTools as online mode.
 *
 * If the Gemma model file is missing (weights not pushed yet), a deterministic
 * rule-based parser takes over — the demo NEVER dies. Same tool calls either way.
 *
 * Model file search order:
 *   1. /data/local/tmp/llm/gemma.task            (adb push, survives reinstalls)
 *   2. <app-files>/models/gemma.task             (bundled/downloaded)
 */
class OfflineModelManager(
    private val context: Context,
    private val catalog: CatalogRepository
) {
    private var llm: LlmInference? = null
    var modelLoaded = false
        private set

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (llm != null) return@withContext
        val modelFile = MODEL_PATHS
            .map { File(it.replace("{files}", context.filesDir.absolutePath)) }
            .firstOrNull { it.exists() }
        if (modelFile == null) {
            Log.w(TAG, "No Gemma model file found. Using rule-based fallback parser.")
            return@withContext
        }
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(512)
                .build()
            llm = LlmInference.createFromOptions(context, options)
            modelLoaded = true
            Log.i(TAG, "Gemma loaded from ${modelFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Gemma load failed, falling back to rule-based parser", e)
        }
    }

    /** A parsed billing action, model- and rule-parser-agnostic. */
    sealed class Action {
        data class AddItem(val itemName: String, val quantity: Double) : Action()
        object Summary : Action()
        object RemoveLast : Action()
        object ClearBill : Action()
        data class Unknown(val raw: String) : Action()
    }

    suspend fun parseUtterance(utterance: String): List<Action> = withContext(Dispatchers.Default) {
        val commands = parseCommands(utterance)
        if (commands != null) return@withContext commands
        // Try Gemma first, fall back to rules
        val viaModel = llm?.let { parseWithGemma(it, utterance) }
        viaModel ?: parseWithRules(utterance)
    }

    // ----- command detection (khatam / hatao / nayi bill) -----
    private fun parseCommands(utterance: String): List<Action>? {
        val u = utterance.lowercase().trim()
        val summaryWords = listOf("khatam", "khatm", "done", "bas", "total", "mugiyitu")
        val removeWords = listOf("hatao", "remove", "galat", "cancel", "tegeyiri")
        val clearWords = listOf("nayi bill", "naya bill", "new bill", "naya customer", "saaf karo", "clear")
        if (clearWords.any { u.contains(it) }) return listOf(Action.ClearBill)
        if (removeWords.any { u.contains(it) }) return listOf(Action.RemoveLast)
        if (summaryWords.any { u == it || u.contains(it) }) return listOf(Action.Summary)
        return null
    }

    // ----- Gemma structured parsing -----
    private fun parseWithGemma(model: LlmInference, utterance: String): List<Action>? {
        return try {
            val prompt = """
                You convert Indian shopkeeper orders to structured lines.
                Order may be Hindi, Kannada, English or mixed.
                Hindi numbers: ek=1 do=2 teen=3 char=4 paanch=5 adha=0.5 paav=0.25 dedh=1.5.
                Output ONLY lines of the form: ITEM|QUANTITY
                One line per item. No other text.

                Order: "do kilo cheeni aur ek Parle-G"
                cheeni|2
                Parle-G|1

                Order: "$utterance"
            """.trimIndent()
            val response = model.generateResponse(prompt) ?: return null
            val actions = response.lines()
                .mapNotNull { line ->
                    val parts = line.trim().split("|")
                    if (parts.size == 2) {
                        val qty = parts[1].trim().toDoubleOrNull() ?: return@mapNotNull null
                        Action.AddItem(parts[0].trim(), qty)
                    } else null
                }
            actions.ifEmpty { null }
        } catch (e: Exception) {
            Log.w(TAG, "Gemma parse failed, using rules", e)
            null
        }
    }

    // ----- deterministic fallback: split on connectors, extract qty + item -----
    private suspend fun parseWithRules(utterance: String): List<Action> {
        val chunks = utterance.lowercase()
            .split(Regex("""\baur\b|\band\b|\bmattu\b|,"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (chunks.isEmpty()) return listOf(Action.Unknown(utterance))

        return chunks.map { chunk ->
            val qty = QuantityParser.parse(chunk)
            val namePart = QuantityParser.stripQuantity(chunk)
            if (namePart.isBlank()) Action.Unknown(chunk)
            else Action.AddItem(namePart, qty)
        }
    }

    fun close() {
        llm?.close()
        llm = null
        modelLoaded = false
    }

    companion object {
        private const val TAG = "KhaataOffline"
        val MODEL_PATHS = listOf(
            "/data/local/tmp/llm/gemma.task",
            "{files}/models/gemma.task"
        )
    }
}
