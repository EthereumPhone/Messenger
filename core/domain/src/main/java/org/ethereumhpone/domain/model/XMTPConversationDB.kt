package org.ethereumhpone.domain.model

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumhpone.database.model.Message

class XMTPConversationDB(
    private val sharedPreferences: SharedPreferences
) {
    // Map to hold MutableStateFlows for each conversation
    private val conversationMessagesFlows = mutableMapOf<String, MutableStateFlow<List<Message>>>()

    fun isConversationXMTP(conversationId: String): Boolean =
        sharedPreferences.getBoolean("c_$conversationId", false)

    fun setConversationXMTP(conversationId: String) {
        sharedPreferences.edit().putBoolean("c_$conversationId", true).apply()
    }

    // Returns a StateFlow of messages for a given conversationId
    fun getMessagesXMTP(conversationId: String): StateFlow<List<Message>> {
        // Initialize flow if not already present
        if (conversationId !in conversationMessagesFlows) {
            val messages = loadMessagesFromPreferences(conversationId)
            conversationMessagesFlows[conversationId] = MutableStateFlow(messages)
        }
        return conversationMessagesFlows[conversationId]!!
    }

    // Upserts messages and updates the flow
    fun upsertMessagesXMTP(conversationId: String, messages: List<Message>) {
        val key = "m_$conversationId"
        val existingMessagesJsonSet = sharedPreferences.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()

        // Create a map for quick lookup of existing messages by their ID
        val existingMessagesMap = existingMessagesJsonSet
            .map { Json.decodeFromString<Message>(it) }
            .associateBy { it.id }
            .toMutableMap()

        // Upsert messages
        messages.forEach { message ->
            existingMessagesMap[message.id] = message
        }

        // Convert updated map back to JSON set
        val updatedMessagesJsonSet = existingMessagesMap.values.map {
            Json.encodeToString(it)
        }.toSet()

        // Save back to SharedPreferences
        sharedPreferences.edit()
            .putStringSet(key, updatedMessagesJsonSet)
            .apply()

        // Update the StateFlow with the new list of messages
        val updatedMessagesList = existingMessagesMap.values.toList()
        conversationMessagesFlows[conversationId]?.value = updatedMessagesList
    }

    fun getLatestMessage(conversationId: String): Message? {
        val messages = getMessagesXMTP(conversationId).value
        return messages.maxByOrNull { it.date } // Replace 'timestamp' with the actual field representing time in Message
    }

    // Helper function to load messages from SharedPreferences
    private fun loadMessagesFromPreferences(conversationId: String): List<Message> {
        val messagesJsonSet =
            sharedPreferences.getStringSet("m_$conversationId", emptySet()) ?: emptySet()
        return messagesJsonSet.map { json ->
            Json.decodeFromString<Message>(json)
        }
    }
}
