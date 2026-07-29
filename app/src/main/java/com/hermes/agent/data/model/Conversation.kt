package com.hermes.agent.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a conversation/session with the Hermes Agent.
 */
data class Conversation(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("title")
    val title: String = "New Conversation",

    @SerializedName("messages")
    val messages: List<Message> = emptyList(),

    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @SerializedName("model")
    val model: String = "default",

    @SerializedName("unread_count")
    val unreadCount: Int = 0
) {
    /** Get a preview of the last message for display */
    val lastMessagePreview: String
        get() = messages.lastOrNull()?.content
            ?.take(80)
            ?.replace("\n", " ")
            ?: "No messages"

    /** Format the last update time relative to now */
    val formattedTime: String
        get() {
            val now = System.currentTimeMillis()
            val diff = now - updatedAt
            return when {
                diff < 60_000 -> "Just now"
                diff < 3_600_000 -> "${diff / 60_000}m ago"
                diff < 86_400_000 -> "${diff / 3_600_000}h ago"
                else -> "${diff / 86_400_000}d ago"
            }
        }
}
