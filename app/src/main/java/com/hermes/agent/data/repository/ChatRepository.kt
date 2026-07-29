package com.hermes.agent.data.repository

import android.util.Log
import com.hermes.agent.data.model.*
import com.hermes.agent.network.ApiConfig
import com.hermes.agent.network.HermesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Repository layer for all Hermes Agent data operations.
 * Abstracts the API service and manages connection state.
 */
class ChatRepository {

    companion object {
        private const val TAG = "ChatRepository"
    }

    private var apiService: HermesApiService? = null
    private var currentConfig: ApiConfig? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    /** Connection states */
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    /**
     * Connect to the Hermes Agent server with the given config.
     */
    suspend fun connect(config: ApiConfig): Result<ServerStatus> = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.CONNECTING
        currentConfig = config

        try {
            apiService = HermesApiService.create(config)

            // Try to verify connection
            val healthResponse = apiService!!.getHealth()
            if (healthResponse.isSuccessful) {
                val status = healthResponse.body()?.data
                _connectionState.value = ConnectionState.CONNECTED
                Log.d(TAG, "Connected to Hermes Agent: ${status?.version}")
                return@withContext Result.success(status ?: ServerStatus("ok", "unknown", 0))
            } else {
                _connectionState.value = ConnectionState.ERROR
                return@withContext Result.failure(
                    Exception("Server returned ${healthResponse.code()}: ${healthResponse.message()}")
                )
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            Log.e(TAG, "Connection failed", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Authenticate with the server.
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val service = apiService
                    ?: return@withContext Result.failure(Exception("Not connected"))

                val response = service.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        // Update config with token
                        currentConfig = currentConfig?.copy(
                            username = username,
                            password = password,
                            token = loginData.token
                        )
                        Log.d(TAG, "Login successful for user: $username")
                        return@withContext Result.success(loginData)
                    }
                }
                return@withContext Result.failure(
                    Exception(response.errorBody()?.string() ?: "Login failed")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                return@withContext Result.failure(e)
            }
        }

    /**
     * Load all conversations from the server.
     */
    suspend fun loadConversations(): Result<List<Conversation>> = withContext(Dispatchers.IO) {
        try {
            val service = apiService ?: return@withContext Result.failure(Exception("Not connected"))
            val response = service.getConversations()
            if (response.isSuccessful) {
                val convos = response.body()?.data ?: emptyList()
                _conversations.value = convos
                return@withContext Result.success(convos)
            } else {
                return@withContext Result.failure(
                    Exception("Failed to load conversations: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load conversations", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Send a message and get a response.
     */
    suspend fun sendMessage(
        message: String,
        conversationId: String? = null
    ): Result<Conversation> = withContext(Dispatchers.IO) {
        try {
            val service = apiService ?: return@withContext Result.failure(Exception("Not connected"))

            val request = ChatRequest(
                message = message,
                conversationId = conversationId,
                model = currentConfig?.defaultModel,
                stream = false
            )

            val response = service.sendMessage(request)
            if (response.isSuccessful) {
                val conversation = response.body()?.data
                if (conversation != null) {
                    return@withContext Result.success(conversation)
                }
            }
            return@withContext Result.failure(
                Exception(response.errorBody()?.string() ?: "Failed to send message")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Delete a conversation.
     */
    suspend fun deleteConversation(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = apiService ?: return@withContext Result.failure(Exception("Not connected"))
            val response = service.deleteConversation(id)
            if (response.isSuccessful) {
                // Remove from local list
                _conversations.value = _conversations.value.filter { it.id != id }
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(
                    Exception("Failed to delete conversation: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete conversation", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Disconnect from the server.
     */
    fun disconnect() {
        apiService = null
        currentConfig = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _conversations.value = emptyList()
        Log.d(TAG, "Disconnected")
    }

    /** Check if we're currently connected */
    val isConnected: Boolean
        get() = _connectionState.value == ConnectionState.CONNECTED
}
