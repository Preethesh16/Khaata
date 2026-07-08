package com.khaata.app.agent

import com.google.firebase.Firebase
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import org.json.JSONObject

/**
 * ONLINE brain: Gemini via Firebase AI Logic — direct from the app,
 * no backend server. The model receives the transcript and returns a
 * strict-JSON action plan that maps 1:1 onto the agent tools.
 *
 * (Person A upgrade path: swap generateContent for the Live API
 * liveModel session with native audio + FunctionDeclarations. The
 * action contract below stays identical.)
 */
class LiveApiManager {

    private val model: GenerativeModel? = runCatching {
        Firebase.vertexAI.generativeModel(
            modelName = "gemini-2.0-flash",
            systemInstruction = content { text(SYSTEM_PROMPT) }
        )
    }.getOrNull()

    val isAvailable: Boolean get() = model != null

    /** Transcript -> JSON action plan via Gemini. Throws on network/API failure so caller can fall back offline. */
    suspend fun parseUtterance(transcript: String): Pair<List<AgentAction>, String> {
        val m = model ?: error("Gemini model not initialised (check google-services.json)")
        val response = m.generateContent("Shopkeeper said: \"$transcript\"\nReturn the JSON action plan.")
        val text = response.text ?: error("Empty Gemini response")
        return parseActionJson(text)
    }

    companion object {
        const val SYSTEM_PROMPT = """
You are Khaata, a billing assistant for small Indian kirana shops.
The shopkeeper speaks in Hindi, Kannada, or English (often code-mixed).
Identify items and quantities and reply ONLY with strict JSON, no markdown fences:

{"actions":[{"type":"add","item":"<english item name>","qty":<number>}],"reply":"<short Hindi confirmation>"}

Action types:
- {"type":"add","item":"...","qty":N}      add item to bill
- {"type":"remove_last"}                    shopkeeper said hatao/remove/galat
- {"type":"clear"}                          naya bill / new customer
- {"type":"summary"}                        khatam / done / bas / total
- {"type":"check_stock","item":"..."}       stock kitna hai
Rules:
- Hindi numbers: ek=1 do=2 teen=3 char=4 paanch=5 adha=0.5 paav=0.25 dedh=1.5 dhai=2.5
- Multiple items in one sentence => multiple add actions in order
- "reply" must be short spoken Hindi, e.g. "do kilo cheeni add kiya"
- If nothing matches, return {"actions":[],"reply":"samajh nahi aaya, dobara boliye"}
"""

        /** Shared JSON->actions parser (also used by the offline Gemma path). */
        fun parseActionJson(raw: String): Pair<List<AgentAction>, String> {
            val cleaned = raw.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            require(start >= 0 && end > start) { "No JSON object in model output" }
            val obj = JSONObject(cleaned.substring(start, end + 1))
            val actions = mutableListOf<AgentAction>()
            val arr = obj.optJSONArray("actions")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val a = arr.getJSONObject(i)
                    when (a.optString("type")) {
                        "add" -> actions += AgentAction.AddItem(
                            a.optString("item"),
                            a.optDouble("qty", 1.0)
                        )
                        "remove_last" -> actions += AgentAction.RemoveLast
                        "clear" -> actions += AgentAction.ClearBill
                        "summary" -> actions += AgentAction.Summary
                        "check_stock" -> actions += AgentAction.CheckStock(a.optString("item"))
                        else -> actions += AgentAction.Unknown(a.toString())
                    }
                }
            }
            return actions to obj.optString("reply")
        }
    }
}
