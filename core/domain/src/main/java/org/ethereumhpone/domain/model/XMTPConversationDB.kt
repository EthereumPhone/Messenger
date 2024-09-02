package org.ethereumhpone.domain.model

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.Conversation
import org.xmtp.android.library.Client

class XMTPConversationDB(
    private val sharedPreferences: SharedPreferences
) {
    // Map to hold MutableStateFlows for each conversation, keyed by Conversation ID
    private val conversationMessagesFlows: MutableMap<String, MutableStateFlow<List<Message>>> = mutableMapOf()
    private val conversationFlows: MutableMap<String, MutableStateFlow<Conversation?>> = mutableMapOf()

    // Flow to hold all conversations
    private val conversationsFlow: MutableStateFlow<List<Conversation>> = MutableStateFlow(emptyList())

    private val xmtpConversations: HashMap<String, org.xmtp.android.library.Conversation> = hashMapOf()

    init {
        // Load existing conversations from SharedPreferences
        loadConversationsIntoStateFlow()
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

    // Get a Conversation by its ID, returns a StateFlow of the Conversation
    fun getConversationById(conversationId: String): StateFlow<Conversation?> {
        if (!conversationFlows.containsKey(conversationId)) {
            val conversation = conversationsFlow.value.firstOrNull { it.id.toString() == conversationId }
            conversationFlows[conversationId] = MutableStateFlow(conversation)
        }
        val convo = conversationFlows[conversationId]!!
        return convo
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

        // Find the newest message by date
        val newestMessage = updatedMessagesList.maxByOrNull { it.date }

        // Update the conversation's lastMessage to the newest message and set the read flag to true
        if (newestMessage != null) {
            // Update the read flag of the newest message to true
            val updatedMessage = newestMessage.copy(read = true)

            // Upsert the updated message with read flag set to true
            existingMessagesMap[updatedMessage.id] = updatedMessage

            // Update the conversation's lastMessage to the newest message
            val updatedConversation = conversation.copy(lastMessage = updatedMessage, unknown = conversation.unknown)
            upsertConversation(updatedConversation)

            // Save the updated messages with read flag to SharedPreferences
            val finalMessagesJsonSet = existingMessagesMap.values.map {
                Json.encodeToString(it)
            }.toSet()

            sharedPreferences.edit()
                .putStringSet(key, finalMessagesJsonSet)
                .apply()

            // Update the StateFlow with the final list of messages
            conversationMessagesFlows[conversationId]?.value = existingMessagesMap.values.toList()
        }
    }

    fun updateConversationReadStatus(conversationId: String, isRead: Boolean = false) {
        val currentConversations = conversationsFlow.value.toMutableList()
        val index = currentConversations.indexOfFirst { it.id.toString() == conversationId }
        if (index >= 0) {
            val conversation = currentConversations[index]
            val lastMessage = conversation.lastMessage

            // Update the lastMessage's read status
            val updatedLastMessage = lastMessage?.copy(read = isRead)

            // Update the conversation with the new lastMessage
            val updatedConversation = conversation.copy(lastMessage = updatedLastMessage)
            upsertConversation(updatedConversation)
        } else {
            // Handle the case where the conversation is not found
            throw IllegalArgumentException("Conversation with ID $conversationId not found.")
        }
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

        // Update the specific conversation flow
        val conversationId = conversation.id.toString()
        if (!conversationFlows.containsKey(conversationId)) {
            conversationFlows[conversationId] = MutableStateFlow(conversation)
        } else {
            conversationFlows[conversationId]?.value = conversation
        }
    }

    fun upsertConversationInSync(conversation: Conversation) {
        val currentConversations = conversationsFlow.value.toMutableList()
        val index = currentConversations.indexOfFirst { it.id == conversation.id }
        if (index >= 0) {
            currentConversations[index] = conversation.copy(unknown = currentConversations[index].unknown) // Update existing
        } else {
            currentConversations.add(conversation) // Add new
        }
        // Serialize and save the conversations
        val serializedConversations = currentConversations.map { Json.encodeToString(it) }.toSet()
        sharedPreferences.edit()
            .putStringSet("conversations", serializedConversations)
            .apply()
        conversationsFlow.value = currentConversations // Update StateFlow

        // Update the specific conversation flow
        val conversationId = conversation.id.toString()
        if (!conversationFlows.containsKey(conversationId)) {
            conversationFlows[conversationId] = MutableStateFlow(conversation)
        } else {
            conversationFlows[conversationId]?.value = conversation
        }
    }

    // Returns a StateFlow of all conversations
    fun getAllConversations(): StateFlow<List<Conversation>> = conversationsFlow

    // Function to modify a Conversation's variable and upsert it again
    fun markConversationAccepted(conversationId: String) {
        val currentConversations = conversationsFlow.value.toMutableList()
        val index = currentConversations.indexOfFirst { it.id.toString() == conversationId }
        if (index >= 0) {
            val updatedConversation = currentConversations[index].copy(unknown = false)
            upsertConversation(updatedConversation)
        } else {
            // Handle the case where the conversation is not found
            throw IllegalArgumentException("Conversation with ID $conversationId not found.")
        }
    }

    // Method to check if a conversation ID is saved in the XMTP conversations
    fun isConversationInXMTP(conversationId: String): Boolean {
        return conversationsFlow.value.any { it.id.toString() == conversationId }
    }

    // Helper function to load conversations into StateFlow from SharedPreferences
    private fun loadConversationsIntoStateFlow() {
        val conversations = loadConversationsFromPreferences()
        conversationsFlow.value = conversations

        // Initialize conversation flows for loaded conversations
        conversations.forEach { conversation ->
            val conversationId = conversation.id.toString()
            if (!conversationMessagesFlows.containsKey(conversationId)) {
                val messages = loadMessagesFromPreferences(conversationId)
                conversationMessagesFlows[conversationId] = MutableStateFlow(messages)
            }
            if (!conversationFlows.containsKey(conversationId)) {
                conversationFlows[conversationId] = MutableStateFlow(conversation)
            }
        }
    }

    suspend fun getOrCreateXMTPConversation(address: String, client: Client): org.xmtp.android.library.Conversation {
        return xmtpConversations.getOrPut(address) {
            client.conversations.newConversation(address)
        }
    }

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
