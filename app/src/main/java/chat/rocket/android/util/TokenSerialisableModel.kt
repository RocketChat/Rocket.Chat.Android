package chat.rocket.android.util

import java.io.Serializable

class TokenSerialisableModel(
    private val firstParam: String,
    private val secondParam: String,
    private val thirdParam: String = ""
) :
    Serializable {
    companion object {
        private val serialVersionUID: Long = 101L
    }

    private val tokenUserId: String
    private val authToken: String
    private val serverUrl: String

    init {
        tokenUserId = firstParam
        authToken = secondParam
        serverUrl = thirdParam
    }

    fun getFirst(): String {
        return tokenUserId
    }

    fun getSecond(): String {
        return authToken
    }

    fun getThird(): String {
        return serverUrl
    }
}