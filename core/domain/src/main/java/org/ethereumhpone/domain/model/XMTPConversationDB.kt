package org.ethereumhpone.domain.model

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Conversation

class XMTPConversationDB(
    private val sharedPreferences: SharedPreferences
) {
    // Map to hold MutableStateFlows for each conversation, keyed by Conversation ID
    private val conversationMessagesFlows: MutableMap<String, MutableStateFlow<List<Message>>> = mutableMapOf()

    // Flow to hold all conversations
    private val conversationsFlow: MutableStateFlow<List<Conversation>> = MutableStateFlow(emptyList())

    init {
        // Load existing conversations from SharedPreferences
        val existingConversations = loadConversationsFromPreferences()
        conversationsFlow.value = existingConversations

        // Initialize conversation flows for loaded conversations
        existingConversations.forEach { conversation ->
            val conversationId = conversation.id.toString()
            if (!conversationMessagesFlows.containsKey(conversationId)) {
                val messages = loadMessagesFromPreferences(conversationId)
                conversationMessagesFlows[conversationId] = MutableStateFlow(messages)
            }
        }
    }

    fun isConversationXMTP(conversation: Conversation): Boolean =
        sharedPreferences.getBoolean("c_${conversation.id}", false)

    fun setConversationXMTP(conversation: Conversation) {
        sharedPreferences.edit().putBoolean("c_${conversation.id}", true).apply()
    }

    // Returns a StateFlow of messages for a given Conversation
    fun getMessagesXMTP(conversation: Conversation): StateFlow<List<Message>> {
        val conversationId = conversation.id.toString()
        // Initialize flow if not already present
        if (!conversationMessagesFlows.containsKey(conversationId)) {
            val messages = loadMessagesFromPreferences(conversationId)
            conversationMessagesFlows[conversationId] = MutableStateFlow(messages)
        }
        return conversationMessagesFlows[conversationId]!!
    }

    // Upserts messages and updates the flow for a given Conversation
    fun upsertMessagesXMTP(conversation: Conversation, messages: List<Message>) {
        val conversationId = conversation.id.toString()
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

    // Adds or updates a Conversation and updates the flow
    fun upsertConversation(conversation: Conversation) {
        val currentConversations = conversationsFlow.value.toMutableList()
        val index = currentConversations.indexOfFirst { it.id == conversation.id }
        if (index >= 0) {
            currentConversations[index] = conversation // Update existing
        } else {
            currentConversations.add(conversation) // Add new
        }
        // Serialize and save the conversations
        val serializedConversations = currentConversations.map { Json.encodeToString(it) }.toSet()
        sharedPreferences.edit()
            .putStringSet("conversations", serializedConversations)
            .apply()
        conversationsFlow.value = currentConversations // Update StateFlow
    }

    // Returns a StateFlow of all conversations
    fun getAllConversations(): StateFlow<List<Conversation>> = conversationsFlow

    // Helper function to load conversations from SharedPreferences
    private fun loadConversationsFromPreferences(): List<Conversation> {
        val conversationsJsonSet = sharedPreferences.getStringSet("conversations", emptySet()) ?: emptySet()
        return conversationsJsonSet.map { json ->
            Json.decodeFromString<Conversation>(json)
        }
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
