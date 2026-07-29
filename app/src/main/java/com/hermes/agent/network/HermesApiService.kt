package com.hermes.agent.network

import com.hermes.agent.data.model.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Retrofit API service interface for Hermes Agent REST API.
 */
interface HermesApiService {

    // ==================== Health & Status ====================

    @GET("api/health")
    suspend fun getHealth(): Response<ApiResponse<ServerStatus>>

    // ==================== Authentication ====================

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/auth/verify")
    suspend fun verifyToken(): Response<ApiResponse<LoginResponse>>

    // ==================== Conversations ====================

    @GET("api/conversations")
    suspend fun getConversations(): Response<ApiResponse<List<Conversation>>>

    @GET("api/conversations/{id}")
    suspend fun getConversation(@Path("id") id: String): Response<ApiResponse<Conversation>>

    @DELETE("api/conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ==================== Chat ====================

    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ApiResponse<Conversation>>

    @POST("api/chat/stream")
    suspend fun sendMessageStream(@Body request: ChatRequest): Response<ResponseBody>

    // ==================== Models ====================

    @GET("api/models")
    suspend fun getModels(): Response<ApiResponse<List<String>>>

    companion object {
        /**
         * Create a HermesApiService instance from the given API config.
         */
        fun create(config: ApiConfig): HermesApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .apply {
                    // Add auth interceptor if we have credentials
                    if (config.hasToken) {
                        addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer ${config.token}")
                                .build()
                            chain.proceed(request)
                        }
                    } else if (config.hasAuth) {
                        // Basic auth
                        val credentials = okhttp3.Credentials.basic(config.username, config.password)
                        addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", credentials)
                                .build()
                            chain.proceed(request)
                        }
                    }
                    addInterceptor(logging)
                }
                .build()

            val baseUrl = if (config.serverUrl.endsWith("/")) {
                config.serverUrl
            } else {
                "${config.serverUrl}/"
            }

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HermesApiService::class.java)
        }
    }
}
