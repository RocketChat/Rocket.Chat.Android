package chat.rocket.android.fragment.chatroom.dialog

import android.content.Context
import android.os.Handler
import android.util.Log
import chat.rocket.android.R
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.core.SyncState
import chat.rocket.core.models.Message
import chat.rocket.core.models.Room
import chat.rocket.core.models.User
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp

class RoomDialogPresenter(val context: Context, val view: RoomDialogContract.View): RoomDialogContract.Presenter {
    val mainHandler = Handler(context.mainLooper)

    override fun getDataSet(roomId: String,
                            roomType: String,
                            hostname: String,
                            token: String,
                            userId: String,
                            action: Int) {
        when (action) {
            R.id.action_pinned_messages -> {
                getPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_favorite_messages -> {
                getFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_file_list -> {
                getFileList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_member_list -> {
                getMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
        }
    }

    private fun getPinnedMessages(roomId: String,
                                  roomType: String,
                                  hostname: String,
                                  token: String,
                                  userId: String) {
        OkHttpHelper.getClient()
                .newCall(getRequestForPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        view.showMessage(e.printStackTrace().toString())
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val jSONObject = JSONObject(response.body()?.string())
                            val messagesJSONArray = jSONObject.get("messages") as JSONArray
                            val messagesJSONArrayLength = messagesJSONArray.length()

                            val dataSet = ArrayList<Message>(messagesJSONArrayLength)
                            (0 until messagesJSONArrayLength).mapTo(dataSet) {
                                val userJSONArray = JSONArray()
                                userJSONArray.put(messagesJSONArray.getJSONObject(it).get("u"))

                                val user = User.builder()
                                        .setId(userJSONArray.getJSONObject(0).optString("_id"))
                                        .setUsername(userJSONArray.getJSONObject(0).optString("username"))
                                        .setUtcOffset(0.0)
                                        .build()

                                val timestampString = messagesJSONArray.getJSONObject(it).optString("ts")
                                val timestamp = if (timestampString.isBlank()) { 0 } else {
                                    Timestamp.valueOf(timestampString
                                            .replace("T", " ")
                                            .replace("Z", ""))
                                            .time
                                }

                                val editedAtString = messagesJSONArray.getJSONObject(it).optString("_updatedAt")
                                val editedAt = if (editedAtString.isBlank()) { 0 } else {
                                    Timestamp.valueOf(editedAtString
                                            .replace("T", " ")
                                            .replace("Z", ""))
                                            .time
                                }

                                Message.builder()
                                        .setId(messagesJSONArray.getJSONObject(it).optString("_id"))
                                        .setRoomId(messagesJSONArray.getJSONObject(it).optString("rid"))
                                        .setMessage(messagesJSONArray.getJSONObject(it).optString("msg"))
                                        .setTimestamp(timestamp)
                                        .setEditedAt(editedAt)
                                        .setGroupable(messagesJSONArray.getJSONObject(it).optBoolean("groupable"))
                                        .setUser(user)
                                        .setSyncState(SyncState.SYNCED)
                                        .build()
                            }
                            mainHandler.post { view.showPinnedMessages(dataSet) }
                        } else {
                            mainHandler.post { view.showMessage(context.getString(R.string.dialog_room_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }

    // TODO (need to create a proper POJO)
    private fun getFavoriteMessages(roomId: String,
                                    roomType: String,
                                    hostname: String,
                                    token: String,
                                    userId: String) {
        OkHttpHelper.getClient()
                .newCall(getRequestForFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        view.showMessage(e.printStackTrace().toString())
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val jSONObject = JSONObject(response.body()?.string())
                        } else {
                            mainHandler.post { view.showMessage(context.getString(R.string.dialog_room_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }

    // TODO (need test)
    private fun getFileList(roomId: String,
                            roomType: String,
                            hostname: String,
                            token: String,
                            userId: String) {
        OkHttpHelper.getClient()
                .newCall(getRequestForFileList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        view.showMessage(e.printStackTrace().toString())
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val jSONObject = JSONObject(response.body()?.string())
                            Log.i("REST", "= " + jSONObject)

                            val filesJSONArray = jSONObject.get("files") as JSONArray

                            val filesJSONArrayLength = filesJSONArray.length()
                            val amazonS3JSONArray = JSONArray()
                            for (i in 0 until filesJSONArrayLength) {
                                amazonS3JSONArray.put(filesJSONArray.getJSONObject(i).get("AmazonS3"))
                            }

                            val pathJSONArray = JSONArray()
                            val amazonS3JSONArrayLength = amazonS3JSONArray.length()
                            for (i in 0 until amazonS3JSONArrayLength) {
                                pathJSONArray.put(amazonS3JSONArray.getJSONObject(i).get("path"))
                            }
                            val pathJSONArrayLength = pathJSONArray.length()
                            val dataSet = ArrayList<String>(pathJSONArrayLength)
                            (0 until pathJSONArrayLength).mapTo(dataSet) { pathJSONArray.get(it).toString() }

                            mainHandler.post { view.showFileList(dataSet) }
                        } else {
                            mainHandler.post { view.showMessage(context.getString(R.string.dialog_room_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }

    // TODO (need test)
    private fun getMemberList(roomId: String,
                              roomType: String,
                              hostname: String,
                              token: String,
                              userId: String) {
        OkHttpHelper.getClient()
                .newCall(getRequestForMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        view.showMessage(e.printStackTrace().toString())
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val jSONObject = JSONObject(response.body()?.string())
                            Log.i("REST", "= " + jSONObject)

                            val membersJSONArray = jSONObject.get("members") as JSONArray

                            val total = membersJSONArray.length()
                            val dataSet = ArrayList<String>(total)
                            (0 until total).mapTo(dataSet) { membersJSONArray.get(it).toString() }

                            mainHandler.post { view.showMemberList(dataSet) }
                        } else {
                            mainHandler.post { view.showMessage(context.getString(R.string.dialog_room_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }

    /**
     * Returns an OkHttp3 request for pinned messages list accordingly with the room type.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @return A OkHttp3 request.
     */
    private fun getRequestForPinnedMessages(roomId: String,
                                              roomType: String,
                                              hostname: String,
                                              token: String,
                                              userId: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForMessages(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.addQueryParameter("query", "{\"pinned\":true}")
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an OkHttp3 request for favorite messages list accordingly with the room type.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @return A OkHttp3 request.
     */
    private fun getRequestForFavoriteMessages(roomId: String,
                                      roomType: String,
                                      hostname: String,
                                      token: String,
                                      userId: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForMessages(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.addQueryParameter("query", "{\"starred._id\":{\"\$in\":[\"$userId\"] } }")
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an OkHttp3 request for file list accordingly with the room type.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @return A OkHttp3 request.
     */
    private fun getRequestForFileList(roomId: String,
                                        roomType: String,
                                        hostname: String,
                                        token: String,
                                        userId: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForFileList(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an OkHttp3 request for member list accordingly with the room type.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @return A OkHttp3 request.
     */
    private fun getRequestForMemberList(roomId: String,
                           roomType: String,
                           hostname: String,
                           token: String,
                           userId: String): Request {
        val parsedHttpUrl = HttpUrl.parse(getEndpointUrlForMemberList(roomType, hostname))
                ?.newBuilder()
                ?.addQueryParameter("roomId", roomId)
                ?.build()

        return Request.Builder()
                .url(parsedHttpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns a Rest API endpoint URL for favorite or pinned messages accordingly with the room type.
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint.
     */
    private fun getEndpointUrlForMessages(roomType: String, hostname: String): String  = "https://" +
            hostname.replace("http://", "").replace("https://", "") +
            getRestApiUrlForMessages(roomType)


    /**
     * Returns a Rest API endpoint URL for member list accordingly with the room type.
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint.
     */
    private fun getEndpointUrlForMemberList(roomType: String, hostname: String): String  = "https://" +
            hostname.replace("http://", "").replace("https://", "") +
            getRestApiUrlForMemberList(roomType)

    /**
     * Returns a Rest API endpoint URL for file list. accordingly with the room type
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint.
     */
    private fun getEndpointUrlForFileList(roomType: String, hostname: String): String  = "https://" +
            hostname.replace("http://", "").replace("https://", "") +
            getRestApiUrlForFileList(roomType)

    /**
     * Returns the correspondent Rest API URL accordingly with the room type to get its favorite or pinned messages.
     *
     * REMARK: To see all the REST API calls take a look at https://rocket.chat/docs/developer-guides/rest-api/.
     *
     * @param roomType The type of the room.
     * @return A Rest API URL or null if the room type does not match.
     */
    private fun getRestApiUrlForMessages(roomType: String): String? {
        var restApiUrl: String? = null
        when (roomType) {
            Room.TYPE_CHANNEL -> restApiUrl = "/api/v1/channels.messages"
            Room.TYPE_PRIVATE -> restApiUrl=  "/api/v1/groups.messages"
            Room.TYPE_DIRECT_MESSAGE -> restApiUrl = "/api/v1/dm.messages"
        }
        return restApiUrl
    }

    /**
     * Returns the correspondent Rest API URL accordingly with the room type to get its file list.
     *
     * REMARK: To see all the REST API calls take a look at https://rocket.chat/docs/developer-guides/rest-api/.
     *
     * @param roomType The type of the room.
     * @return A Rest API URL or null if the room type does not match.
     */
    private fun getRestApiUrlForFileList(roomType: String): String? {
        var restApiUrl: String? = null
        when (roomType) {
            Room.TYPE_CHANNEL -> restApiUrl = "/api/v1/channels.files"
            Room.TYPE_PRIVATE -> restApiUrl=  "/api/v1/groups.files"
            Room.TYPE_DIRECT_MESSAGE -> restApiUrl = "/api/v1/dm.files"
        }
        return restApiUrl
    }

    /**
     * Returns the correspondent Rest API URL accordingly with the room type to get its members list.
     *
     * REMARK: To see all the REST API calls take a look at https://rocket.chat/docs/developer-guides/rest-api/.
     *
     * @param roomType The type of the room.
     * @return A Rest API URL or null if the room type does not match.
     */
    private fun getRestApiUrlForMemberList(roomType: String): String? {
        var restApiUrl: String? = null
        when (roomType) {
            Room.TYPE_CHANNEL -> restApiUrl = "/api/v1/channels.members"
            Room.TYPE_PRIVATE -> restApiUrl=  "/api/v1/groups.members"
            Room.TYPE_DIRECT_MESSAGE -> restApiUrl = "/api/v1/dm.members"
        }
        return restApiUrl
    }
}