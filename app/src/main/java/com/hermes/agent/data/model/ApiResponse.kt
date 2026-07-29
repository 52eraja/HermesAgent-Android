package com.hermes.agent.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper for Hermes Agent REST API.
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null
)

/**
 * Request body for sending a chat message.
 */
data class ChatRequest(
    @SerializedName("message")
    val message: String,

    @SerializedName("conversation_id")
    val conversationId: String? = null,

    @SerializedName("model")
    val model: String? = null,

    @SerializedName("stream")
    val stream: Boolean = true
)

/**
 * Streaming chat event from SSE.
 */
data class ChatStreamEvent(
    @SerializedName("type")
    val type: String = "", // "token", "done", "error"

    @SerializedName("content")
    val content: String = "",

    @SerializedName("conversation_id")
    val conversationId: String? = null
)

/**
 * Login request body.
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

/**
 * Login response with token.
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String = "",

    @SerializedName("user")
    val user: UserInfo? = null
)

/**
 * User information from server.
 */
data class UserInfo(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("username")
    val username: String = "",

    @SerializedName("display_name")
    val displayName: String = ""
)

/**
 * Server health/status info.
 */
data class ServerStatus(
    @SerializedName("status")
    val status: String = "",

    @SerializedName("version")
    val version: String = "",

    @SerializedName("uptime")
    val uptime: Long = 0
)
