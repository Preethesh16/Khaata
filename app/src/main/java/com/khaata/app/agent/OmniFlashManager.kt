package com.khaata.app.agent

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import org.json.JSONObject

data class ProductIdentification(
    val productName: String,
    val quantity: Double,
    val unit: String,
    val confidence: Double
)

/**
 * The wildcard: point the camera at a product, Gemini vision identifies it,
 * and it flows into the exact same tool pipeline as voice.
 * Confidence gating: >0.7 auto-add, 0.4–0.7 confirm, <0.4 ask to speak.
 */
class OmniFlashManager {

    private val model: GenerativeModel? = runCatching {
        Firebase.vertexAI.generativeModel(modelName = "gemini-2.0-flash")
    }.getOrNull()

    val isAvailable: Boolean get() = model != null

    suspend fun identifyProduct(frame: Bitmap): ProductIdentification {
        val m = model ?: error("Vision model not initialised")
        val response = m.generateContent(
            content {
                image(frame)
                text(
                    """
                    Look at this product photo from an Indian kirana shop.
                    Return JSON only, no markdown:
                    {"productName":"<common english product name>","quantity":1,"unit":"pkt|kg|piece|litre","confidence":0.0}
                    confidence is 0.0-1.0. If you cannot identify it, use confidence 0.0.
                    """.trimIndent()
                )
            }
        )
        val raw = response.text ?: error("Empty vision response")
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        require(start >= 0 && end > start) { "No JSON in vision output" }
        val obj = JSONObject(cleaned.substring(start, end + 1))
        return ProductIdentification(
            productName = obj.optString("productName"),
            quantity = obj.optDouble("quantity", 1.0),
            unit = obj.optString("unit", "piece"),
            confidence = obj.optDouble("confidence", 0.0)
        )
    }
}
