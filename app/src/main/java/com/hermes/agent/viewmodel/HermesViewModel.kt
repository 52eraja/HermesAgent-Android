package com.hermes.agent.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hermes.agent.data.model.Conversation
import com.hermes.agent.data.model.Message
import com.hermes.agent.data.model.MessageRole
import com.hermes.agent.data.model.MessageStatus
import com.hermes.agent.data.repository.ChatRepository
import com.hermes.agent.data.repository.SettingsRepository
import com.hermes.agent.network.ApiConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI state for the main chat screen.
 */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val currentConversationId: String? = null,
    val isStreaming: Boolean = false,
    val inputText: String = "",
    val error: String? = null
)

/**
 * UI state for connection status.
 */
data class ConnectionUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val serverUrl: String = ApiConfig.DEFAULT_SERVER_URL,
    val serverVersion: String = "",
    val error: String? = null
)

/**
 * UI state for settings screen.
 */
data class SettingsUiState(
    val serverUrl: String = ApiConfig.DEFAULT_SERVER_URL,
    val username: String = "",
    val password: String = "",
    val defaultModel: String = ApiConfig.DEFAULT_MODEL,
    val darkMode: String = "system",
    val isSaved: Boolean = false
)

/**
 * Main ViewModel for the Hermes Agent app.
 * Manages connection, chat, conversations, and settings state.
 */
class HermesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val settingsRepository = SettingsRepository(application)

    // Settings
    private val _settings = MutableStateFlow(SettingsUiState())
    val settings: StateFlow<SettingsUiState> = _settings.asStateFlow()

    // Connection state
    private val _connection = MutableStateFlow(ConnectionUiState())
    val connectionState: StateFlow<ConnectionUiState> = _connection.asStateFlow()

    // Chat state
    private val _chat = MutableStateFlow(ChatUiState())
    val chatState: StateFlow<ChatUiState> = _chat.asStateFlow()

    // Conversations list
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Repository connection state observer
    private var _repoConnectionState = MutableStateFlow(ChatRepository.ConnectionState.DISCONNECTED)

    init {
        // Observe repository connection state
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _repoConnectionState.value = state
                _connection.update {
                    it.copy(
                        isConnected = state == ChatRepository.ConnectionState.CONNECTED,
                        isConnecting = state == ChatRepository.ConnectionState.CONNECTING,
                        error = if (state == ChatRepository.ConnectionState.ERROR) "Connection failed" else null
                    )
                }
            }
        }

        // Observe conversations from repository
        viewModelScope.launch {
            repository.conversations.collect { convos ->
                _conversations.value = convos
            }
        }

        // Load saved settings
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { config ->
                _settings.update {
                    it.copy(
                        serverUrl = config.serverUrl,
                        username = config.username,
                        password = config.password,
                        defaultModel = config.defaultModel
                    )
                }
                _connection.update {
                    it.copy(serverUrl = config.serverUrl)
                }
            }
        }

        // Load dark mode preference
        viewModelScope.launch {
            settingsRepository.darkModeFlow.collect { mode ->
                _settings.update { it.copy(darkMode = mode) }
            }
        }
    }

    // ==================== Connection Actions ====================

    /**
     * Connect to the Hermes Agent server with the current settings.
     */
    fun connect() {
        viewModelScope.launch {
            val s = _settings.value
            val config = ApiConfig(
                serverUrl = s.serverUrl,
                username = s.username,
                password = s.password,
                defaultModel = s.defaultModel
            )

            val result = repository.connect(config)
            result.onSuccess { status ->
                _connection.update {
                    it.copy(
                        isConnected = true,
                        isConnecting = false,
                        serverVersion = status.version,
                        error = null
                    )
                }
                // Try to authenticate
                if (config.hasAuth && !config.hasToken) {
                    login(s.username, s.password)
                }
                // Load conversations
                repository.loadConversations()
            }.onFailure { error ->
                _connection.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                        error = error.message ?: "Connection failed"
                    )
                }
            }
        }
    }

    /**
     * Login to the Hermes Agent server.
     */
    private fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = repository.login(username, password)
            result.onSuccess { loginResponse ->
                // Save updated config with token
                val current = _settings.value
                val config = ApiConfig(
                    serverUrl = current.serverUrl,
                    username = username,
                    password = password,
                    token = loginResponse.token,
                    defaultModel = current.defaultModel
                )
                settingsRepository.saveConfig(config)
                _connection.update { it.copy(error = null) }
                repository.loadConversations()
            }.onFailure { error ->
                _connection.update {
                    it.copy(error = "Login failed: ${error.message}")
                }
            }
        }
    }

    /**
     * Disconnect from the server.
     */
    fun disconnect() {
        repository.disconnect()
        _connection.value = ConnectionUiState(serverUrl = _settings.value.serverUrl)
        _chat.value = ChatUiState()
        _conversations.value = emptyList()
    }

    // ==================== Chat Actions ====================

    /**
     * Update the input text field.
     */
    fun updateInputText(text: String) {
        _chat.update { it.copy(inputText = text) }
    }

    /**
     * Send a chat message.
     */
    fun sendMessage() {
        val text = _chat.value.inputText.trim()
        if (text.isEmpty() || _chat.value.isStreaming) return

        // Add user message to local state
        val userMessage = Message(
            id = "local_${System.currentTimeMillis()}",
            role = MessageRole.USER,
            content = text,
            status = MessageStatus.SENT
        )

        _chat.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isStreaming = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = repository.sendMessage(
                message = text,
                conversationId = _chat.value.currentConversationId
            )

            result.onSuccess { conversation ->
                // Update with server response
                _chat.update {
                    it.copy(
                        messages = conversation.messages,
                        currentConversationId = conversation.id,
                        isStreaming = false
                    )
                }
                // Refresh conversations list
                repository.loadConversations()
            }.onFailure { error ->
                // Add error message
                val errorMessage = Message(
                    id = "error_${System.currentTimeMillis()}",
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ Error: ${error.message}",
                    status = MessageStatus.ERROR
                )
                _chat.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isStreaming = false,
                        error = error.message
                    )
                }
            }
        }
    }

    /**
     * Load a specific conversation's messages.
     */
    fun loadConversation(conversation: Conversation) {
        _chat.update {
            it.copy(
                messages = conversation.messages,
                currentConversationId = conversation.id,
                error = null
            )
        }
    }

    /**
     * Start a new conversation.
     */
    fun newConversation() {
        _chat.value = ChatUiState()
    }

    /**
     * Delete a conversation.
     */
    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            // If this was the active conversation, clear chat
            if (_chat.value.currentConversationId == id) {
                _chat.value = ChatUiState()
            }
        }
    }

    // ==================== Settings Actions ====================

    /**
     * Update settings fields.
     */
    fun updateSettings(
        serverUrl: String? = null,
        username: String? = null,
        password: String? = null,
        defaultModel: String? = null,
        darkMode: String? = null
    ) {
        _settings.update {
            it.copy(
                serverUrl = serverUrl ?: it.serverUrl,
                username = username ?: it.username,
                password = password ?: it.password,
                defaultModel = defaultModel ?: it.defaultModel,
                darkMode = darkMode ?: it.darkMode
            )
        }
    }

    /**
     * Save settings to persistent storage.
     */
    fun saveSettings() {
        viewModelScope.launch {
            val s = _settings.value
            val config = ApiConfig(
                serverUrl = s.serverUrl,
                username = s.username,
                password = s.password,
                defaultModel = s.defaultModel
            )
            settingsRepository.saveConfig(config)
            settingsRepository.setDarkMode(s.darkMode)
            _settings.update { it.copy(isSaved = true) }
        }
    }

    /**
     * Refresh conversations list.
     */
    fun refreshConversations() {
        viewModelScope.launch {
            repository.loadConversations()
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _connection.update { it.copy(error = null) }
        _chat.update { it.copy(error = null) }
    }
}
