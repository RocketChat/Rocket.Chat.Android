package chat.rocket.android.fragment.chatroom.dialog

import android.content.Context
import android.util.Log
import chat.rocket.android.R
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.core.models.Room
import okhttp3.*
import java.io.IOException

class RoomDialogPresenter(val context: Context, val view: RoomDialogContract.View): RoomDialogContract.Presenter {
    override fun getDataSet(roomId: String,
                            roomName: String,
                            roomType: String,
                            hostname: String,
                            token: String,
                            userId: String,
                            action: Int) {
        when (action) {
            R.id.action_pinned_messages -> getPinnedMessages(roomType, hostname, token, userId)
            R.id.action_favorite_messages -> getFavoriteMessages(roomType, hostname, token, userId)
            R.id.action_file_list -> {
                getFileList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId)
            }
            R.id.action_member_list -> getMemberList(roomType, hostname, token, userId)
        }
    }

    private fun getPinnedMessages(roomType: String, hostname: String, token: String, userId: String) {
        view.showPinnedMessages()
    }

    private fun getFavoriteMessages(roomType: String, hostname: String, token: String, userId: String) {
        view.showFavoriteMessages()
    }

    private fun getFileList(roomId: String,
                            roomType: String,
                            hostname: String,
                            token: String,
                            userId: String) {

        OkHttpHelper.getClient()
                .newCall(getRequest(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.i("REST", "FAIL = " + e.message)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        Log.i("REST", "SUCCESS = " + response.body()?.string())
                        val res = response.body()
                    }
                })
        view.showFileList()
    }

    private fun getMemberList(roomType: String, hostname: String, token: String, userId: String) {
        view.showMemberList()
    }

    /**
     * Returns an OkHttp3 request corresponding to the Rest API call.
     *
     * @param roomId The ID of the room.
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @param token The token.
     * @param userId The user Id.
     * @return A OkHttp3 request.
     */
    private fun getRequest(roomId: String,
                           roomType: String,
                           hostname: String,
                           token: String,
                           userId: String): Request {

        val httpUrl = HttpUrl.Builder()
                .scheme("http")
                .host(getEndpointUrl(roomType, hostname))
                .addQueryParameter("roomId", roomId)
                .build()

        return Request.Builder()
                .url(httpUrl)
                .get()
                .addHeader("X-Auth-Token", token)
                .addHeader("X-User-Id", userId)
                .build()
    }

    /**
     * Returns an endpoint URL (without the https or http schemas) corresponding to the Rest API call.
     *
     * @param roomType The type of the room.
     * @param hostname The server hostname.
     * @return A Rest API URL endpoint starting with www.
     */
    private fun getEndpointUrl(roomType: String, hostname: String): String  = "www." +
            hostname.replace("http://", "")
                    .replace("https://", "")
                    .replace("www", "") +
            getRestApiUrlForMemberList(roomType)

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