package chat.rocket.android.api.rest

import chat.rocket.android.helper.UrlHelper
import chat.rocket.core.models.Room
import okhttp3.HttpUrl
import okhttp3.Request

/**
 * Helper class for dealing with Rest API calls.
 *
 * @see <a href="https://rocket.chat/docs/developer-guides/rest-api">https://rocket.chat/docs/developer-guides/rest-api</a>.
 */
object RestApiHelper {

    /**
     * Returns an OkHttp3 request for pinned messages.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @param offset The offset to paging which specifies the first entry to return from a collection.
     * @return An OkHttp3 request.
     */
    fun getRequestForPinnedMessages(roomId: String,
                                    roomType: String,
                                    hostname: String,
                                    token: String,
                                    userId: String,
                                    offset: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForMessages(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.addQueryParameter("query", "{\"pinned\":true}")
                ?.addQueryParameter("offset", offset)
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an OkHttp3 request for favorite messages.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @param offset The offset to paging which specifies the first entry to return from a collection.
     * @return An OkHttp3 request.
     */
    fun getRequestForFavoriteMessages(roomId: String,
                                      roomType: String,
                                      hostname: String,
                                      token: String,
                                      userId: String,
                                      offset: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForMessages(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.addQueryParameter("query", "{\"starred._id\":{\"\$in\":[\"$userId\"] } }")
                ?.addQueryParameter("offset", offset)
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an OkHttp3 request for file list.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @param offset The offset to paging which specifies the first entry to return from a collection.
     * @return An OkHttp3 request.
     */
    fun getRequestForFileList(roomId: String,
                              roomType: String,
                              hostname: String,
                              token: String,
                              userId: String,
                              offset: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForFileList(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.addQueryParameter("offset", offset)
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an OkHttp3 request for member list.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @param offset The offset to paging which specifies the first entry to return from a collection.
     * @return An OkHttp3 request.
     */
    fun getRequestForMemberList(roomId: String,
                                roomType: String,
                                hostname: String,
                                token: String,
                                userId: String,
                                offset: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForMemberList(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.addQueryParameter("offset", offset)
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns a Rest API endpoint URL for favorite or pinned messages accordingly with the room type and the server hostname.
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint.
     */
    fun getEndpointUrlForMessages(roomType: String, hostname: String): String =
            UrlHelper.getSafeHostname(hostname) + getRestApiUrlForMessages(roomType)

    /**
     * Returns a Rest API endpoint URL for file list accordingly with the room type and the server hostname.
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint.
     */
    fun getEndpointUrlForFileList(roomType: String, hostname: String): String =
            UrlHelper.getSafeHostname(hostname) + getRestApiUrlForFileList(roomType)

    /**
     * Returns a Rest API endpoint URL for member list accordingly with the room type and the server hostname.
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint.
     */
    fun getEndpointUrlForMemberList(roomType: String, hostname: String): String =
            UrlHelper.getSafeHostname(hostname) + getRestApiUrlForMemberList(roomType)

    /**
     * Returns the correspondent Rest API URL accordingly with the room type to get its favorite or pinned messages.
     *
     * @param roomType The type of the room.
     * @return A Rest API URL or null if the room type does not match.
     */
    fun getRestApiUrlForMessages(roomType: String): String? {
        var restApiUrl: String? = null
        when (roomType) {
            Room.TYPE_CHANNEL -> restApiUrl = "/api/v1/channels.messages"
            Room.TYPE_PRIVATE -> restApiUrl = "/api/v1/groups.messages"
            Room.TYPE_DIRECT_MESSAGE -> restApiUrl = "/api/v1/dm.messages"
        }
        return restApiUrl
    }

    /**
     * Returns the correspondent Rest API URL accordingly with the room type to get its file list.
     *
     * @param roomType The type of the room.
     * @return A Rest API URL or null if the room type does not match.
     */
    fun getRestApiUrlForFileList(roomType: String): String? {
        var restApiUrl: String? = null
        when (roomType) {
            Room.TYPE_CHANNEL -> restApiUrl = "/api/v1/channels.files"
            Room.TYPE_PRIVATE -> restApiUrl = "/api/v1/groups.files"
            Room.TYPE_DIRECT_MESSAGE -> restApiUrl = "/api/v1/dm.files"
        }
        return restApiUrl
    }

    /**
     * Returns the correspondent Rest API URL accordingly with the room type to get its members list.
     *
     * @param roomType The type of the room.
     * @return A Rest API URL or null if the room type does not match.
     */
    fun getRestApiUrlForMemberList(roomType: String): String? {
        var restApiUrl: String? = null
        when (roomType) {
            Room.TYPE_CHANNEL -> restApiUrl = "/api/v1/channels.members"
            Room.TYPE_PRIVATE -> restApiUrl = "/api/v1/groups.members"
            Room.TYPE_DIRECT_MESSAGE -> restApiUrl = "/api/v1/dm.members"
        }
        return restApiUrl
    }
}