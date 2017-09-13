package chat.rocket.android.fragment.chatroom.dialog

import android.content.Context
import android.util.Log
import chat.rocket.android.R
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.core.models.Room
import okhttp3.*
import java.io.IOException
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject


class RoomDialogPresenter(val context: Context, val view: RoomDialogContract.View): RoomDialogContract.Presenter {
    override fun getDataSet(roomId: String,
                            roomName: String,
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
        // TODO("not implemented")

    }

    private fun getFavoriteMessages(roomId: String,
                                    roomType: String,
                                    hostname: String,
                                    token: String,
                                    userId: String) {
        // TODO("not implemented")

    }

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
                            Log.i("REST", " = " + jSONObject.toString())
                            Log.i("REST", " = " + jSONObject)
                            val filesJSONArray = jSONObject.get("files") as JSONArray

                            val total = filesJSONArray.length()
                            val dataSet = ArrayList<String>(total)
                            (0 until total).mapTo(dataSet) { filesJSONArray.get(it).toString() }
                            view.showFileList(dataSet)
                        } else {
                            // TODO("move to strings.xml")
                            view.showMessage("Response is not successful")
                        }
                    }
                })
    }

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

                            val membersJSONArray = jSONObject.get("members") as JSONArray

                            val total = membersJSONArray.length()
                            val dataSet = ArrayList<String>(total)
                            (0 until total).mapTo(dataSet) { membersJSONArray.get(it).toString() }

                            view.showMemberList(dataSet)
                        } else {
                            // TODO("move to strings.xml")
                            view.showMessage("Response is not successful")
                        }
                    }
                })
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
}