package com.khaata.app.agent

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

/**
 * The wildcard: point the camera at a product, Gemini Flash vision identifies it,
 * and the result flows into the SAME tool pipeline as voice.
 */
class OmniFlashManager {

    data class ProductIdentification(
        val productName: String,
        val quantity: Double,
        val unit: String,
        val confidence: Double
    )

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(MODEL_NAME)
    }

    suspend fun identifyProduct(frame: Bitmap): ProductIdentification? {
        return try {
            val response = model.generateContent(
                content {
                    image(frame)
                    text(
                        """
                        Look at this product photo from an Indian kirana shop.
                        Return JSON only, no markdown fences:
                        {"productName": "common English brand/product name", "quantity": 1, "unit": "pkt|kg|piece|litre", "confidence": 0.0}
                        confidence is 0.0-1.0. If you cannot identify the product, set confidence to 0.0.
                        """.trimIndent()
                    )
                }
            )
            val raw = response.text?.trim()
                ?.removePrefix("```json")?.removePrefix("```")?.removeSuffix("```")?.trim()
                ?: return null
            val json = Json.parseToJsonElement(raw) as? JsonObject ?: return null
            ProductIdentification(
                productName = json["productName"]?.jsonPrimitive?.content ?: return null,
                quantity = json["quantity"]?.jsonPrimitive?.double ?: 1.0,
                unit = json["unit"]?.jsonPrimitive?.content ?: "piece",
                confidence = json["confidence"]?.jsonPrimitive?.double ?: 0.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "product identification failed", e)
            null
        }
    }

    companion object {
        private const val TAG = "KhaataOmniFlash"
        const val MODEL_NAME = "gemini-2.5-flash"
        // Confidence gates (from the master plan):
        const val AUTO_ADD_THRESHOLD = 0.7
        const val CONFIRM_THRESHOLD = 0.4
    }
}
