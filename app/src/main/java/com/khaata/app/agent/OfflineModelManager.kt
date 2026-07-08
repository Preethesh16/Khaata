package com.khaata.app.agent

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.khaata.app.util.QuantityParser
import java.io.File

/**
 * OFFLINE brain: Gemma on-device via MediaPipe LLM Inference (LiteRT).
 *
 * Model file (~2GB INT4 .task) is pushed once to the phone:
 *   adb shell mkdir -p /data/local/tmp/llm
 *   adb push gemma-3n-E4B-it-int4.task /data/local/tmp/llm/
 *
 * If the weights are not present (or Gemma is warming up), we fall back to a
 * deterministic rule-based parser — same AgentAction contract, so the demo
 * NEVER breaks in airplane mode.
 */
class OfflineModelManager(private val context: Context) {

    private var llm: LlmInference? = null
    var engineName: String = "rules"
        private set

    companion object {
        val MODEL_PATHS = listOf(
            "/data/local/tmp/llm/gemma-3n-E4B-it-int4.task",
            "/data/local/tmp/llm/gemma-3n-E2B-it-int4.task",
            "/data/local/tmp/llm/gemma.task"
        )
    }

    /** Call once at app start (background thread) — pre-warms Gemma if weights exist. */
    fun warmUp() {
        if (llm != null) return
        val path = MODEL_PATHS.firstOrNull { File(it).exists() } ?: return
        runCatching {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(path)
                .setMaxTokens(512)
                .build()
            llm = LlmInference.createFromOptions(context, options)
            engineName = "gemma"
        }
    }

    /** Transcript -> action plan, fully on-device. */
    fun parseUtterance(transcript: String): Pair<List<AgentAction>, String> {
        llm?.let { engine ->
            runCatching {
                val out = engine.generateResponse(
                    LiveApiManager.SYSTEM_PROMPT +
                        "\nShopkeeper said: \"$transcript\"\nReturn the JSON action plan."
                )
                return LiveApiManager.parseActionJson(out)
            }
            // Gemma output was malformed — fall through to rules
        }
        return ruleBasedParse(transcript) to ""
    }

    /**
     * Deterministic fallback parser. Handles the whole demo script:
     * "do kilo cheeni, ek Parle-G, teen Maggi" / "hatao" / "khatam" / "naya bill".
     */
    fun ruleBasedParse(transcript: String): List<AgentAction> {
        val t = transcript.lowercase().trim()
        if (t.isBlank()) return emptyList()

        val summaryWords = listOf("khatam", "खतम", "खत्म", "done", "bas", "बस", "total", "टोटल", "mugiyitu")
        val removeWords = listOf("hatao", "हटाओ", "remove", "galat", "गलत", "cancel", "wapas", "तेगि")
        val clearWords = listOf("naya bill", "नया बिल", "new bill", "clear", "saaf", "hosa bill")

        if (clearWords.any { t.contains(it) }) return listOf(AgentAction.ClearBill)
        if (removeWords.any { t.contains(it) }) return listOf(AgentAction.RemoveLast)
        if (summaryWords.any { t.contains(it) }) return listOf(AgentAction.Summary)

        // Split multi-item orders: "do kilo cheeni aur ek parle g" / commas / "and"
        val segments = t.split(Regex("\\s+aur\\s+|\\s+और\\s+|\\s+and\\s+|,|\\s+mattu\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return segments.map { seg ->
            val qty = QuantityParser.parse(seg)
            val name = QuantityParser.stripQuantityWords(seg).ifBlank { seg }
            AgentAction.AddItem(name, qty)
        }
    }
}
