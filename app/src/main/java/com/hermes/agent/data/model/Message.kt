package com.hermes.agent.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a single message in a conversation.
 */
data class Message(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("role")
    val role: MessageRole = MessageRole.USER,

    @SerializedName("content")
    val content: String = "",

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("status")
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageRole {
    @SerializedName("user")
    USER,

    @SerializedName("assistant")
    ASSISTANT,

    @SerializedName("system")
    SYSTEM
}

enum class MessageStatus {
    /** Message is being sent */
    SENDING,

    /** Message was sent successfully */
    SENT,

    /** Message was delivered and seen */
    DELIVERED,

    /** An error occurred */
    ERROR,

    /** Message is being streamed (AI typing) */
    STREAMING
}
