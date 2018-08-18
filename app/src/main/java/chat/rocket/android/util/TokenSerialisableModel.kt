package chat.rocket.android.util

import java.io.Serializable

class TokenSerialisableModel(
    private val tokenId: String,
    private val tokenAuth: String,
    private val urlServer: String = ""
) :
    Serializable {

    private val tokenUserId: String
    private val authToken: String
    private val serverUrl: String

    init {
        tokenUserId = tokenId
        authToken = tokenAuth
        serverUrl = urlServer
    }

    fun getTokenUserId(): String {
        return tokenUserId
    }

    fun getAuthToken(): String {
        return authToken
    }

    fun getServerUrl(): String {
        return serverUrl
    }

    companion object {
        private val serialVersionUID: Long = 101L
    }
}