package com.khaata.app.agent

/**
 * The unified action format. Both engines (Gemini online, Gemma/rules offline)
 * emit the same actions, so the tool layer and UI never care which brain ran.
 */
sealed class AgentAction {
    data class AddItem(val itemName: String, val quantity: Double) : AgentAction()
    data class CheckStock(val itemName: String) : AgentAction()
    object RemoveLast : AgentAction()
    object ClearBill : AgentAction()
    object Summary : AgentAction()
    data class Unknown(val raw: String) : AgentAction()
}

data class AgentResult(
    val spokenReply: String,          // sent to TTS
    val statusMessage: String,        // shown in UI status line
    val billChanged: Boolean = false,
    val showSummary: Boolean = false
)
