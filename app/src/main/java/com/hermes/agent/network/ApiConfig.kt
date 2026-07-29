package com.hermes.agent.network

/**
 * Configuration for the Hermes Agent API connection.
 * Stored in DataStore and loaded at runtime.
 */
data class ApiConfig(
    val serverUrl: String = DEFAULT_SERVER_URL,
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val defaultModel: String = DEFAULT_MODEL
) {
    companion object {
        const val DEFAULT_SERVER_URL = "http://192.168.50.196:9119"
        const val DEFAULT_MODEL = "deepseek-ai/DeepSeek-V3.2"
    }

    /** Whether the config has enough info to connect */
    val isValid: Boolean
        get() = serverUrl.isNotBlank()

    /** Whether authentication credentials are provided */
    val hasAuth: Boolean
        get() = username.isNotBlank() && password.isNotBlank()

    /** Whether we have an auth token */
    val hasToken: Boolean
        get() = token.isNotBlank()
}
