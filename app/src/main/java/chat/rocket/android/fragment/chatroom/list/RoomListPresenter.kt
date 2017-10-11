package chat.rocket.android.fragment.chatroom.list

import android.content.Context
import android.os.Handler
import chat.rocket.android.R
import chat.rocket.android.api.rest.RestApiHelper
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.core.SyncState
import chat.rocket.core.models.Attachment
import chat.rocket.core.models.AttachmentTitle
import chat.rocket.core.models.Message
import chat.rocket.core.models.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 9/22/17.
 */
class RoomListPresenter(val context: Context, val view: RoomListContract.View) : RoomListContract.Presenter {

    override fun requestPinnedMessages(roomId: String,
                                       roomType: String,
                                       hostname: String,
                                       token: String,
                                       userId: String,
                                       offset: Int) {
        view.showWaitingView(true)
        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset.toString()))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (!call.isCanceled) {
                            val message = e.message
                            if (message != null) {
                                showErrorMessage(message)
                            }
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string()
                            if (result != null) {
                                handleMessagesJson(result, true)
                            }
                        } else {
                            showErrorMessage(response.message())
                        }
                    }
                })
    }

    override fun requestFavoriteMessages(roomId: String,
                                         roomType: String,
                                         hostname: String,
                                         token: String,
                                         userId: String,
                                         offset: Int) {
        view.showWaitingView(true)
        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset.toString()))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (!call.isCanceled) {
                            val message = e.message
                            if (message != null) {
                                showErrorMessage(message)
                            }
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string()
                            if (result != null) {
                                handleMessagesJson(result, false)
                            }
                        } else {
                            showErrorMessage(response.message())
                        }
                    }
                })
    }

    override fun requestFileList(roomId: String,
                                 roomType: String,
                                 hostname: String,
                                 token: String,
                                 userId: String,
                                 offset: Int) {
        view.showWaitingView(true)
        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForFileList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset.toString()))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (!call.isCanceled) {
                            val message = e.message
                            if (message != null) {
                                showErrorMessage(message)
                            }
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string()
                            if (result != null) {
                                handleFilesJson(result, hostname)
                            }
                        } else {
                            showErrorMessage(response.message())
                        }
                    }
                })
    }

    override fun requestMemberList(roomId: String,
                                   roomType: String,
                                   hostname: String,
                                   token: String,
                                   userId: String,
                                   offset: Int) {
        view.showWaitingView(true)
        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId,
                        offset.toString()))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (!call.isCanceled) {
                            val message = e.message
                            if (message != null) {
                                showErrorMessage(message)
                            }
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val result = response.body()?.string()
                            if (result != null) {
                                handleMembersJson(result)
                            }
                        } else {
                            showErrorMessage(response.message())
                        }
                    }
                })
    }

    override fun cancelRequest() {
        OkHttpHelper.getClient().dispatcher().cancelAll()
    }

    private fun handleMessagesJson(json: String, isPinnedMessage: Boolean) {
        try {
            val jSONObject = JSONObject(json)
            val messagesJSONArray = jSONObject.getJSONArray("messages")

            val total = messagesJSONArray.length()
            val dataSet = ArrayList<Message>(total)
            (0 until total).mapTo(dataSet) {
                val messageJsonObject = messagesJSONArray.getJSONObject(it)
                val userJsonObject = messageJsonObject.getJSONObject("u")

                Message.builder()
                        .setId(messageJsonObject.optString("_id"))
                        .setRoomId(messageJsonObject.optString("rid"))
                        .setMessage(messageJsonObject.optString("msg"))
                        .setUser(getUserFromJsonObject(userJsonObject))
                        .setTimestamp(getLongTimestamp(messageJsonObject.optString("ts")))
                        .setEditedAt(getLongTimestamp(messageJsonObject.optString("_updatedAt")))
                        .setGroupable(messageJsonObject.optBoolean("groupable"))
                        .setSyncState(SyncState.SYNCED)
                        .build()
            }

            if (dataSet.isEmpty() && !hasItem) {
                showEmptyViewMessage(context.getString(R.string.fragment_room_list_no_favorite_message_to_show))
            } else {
                if (dataSet.isNotEmpty()) {
                    hasItem = true
                    if (isPinnedMessage) {
                        showPinnedMessageList(dataSet, jSONObject.optString("total"))
                    } else {
                        showFavoriteMessageList(dataSet, jSONObject.optString("total"))
                    }
                }
            }
        } catch (exception: JSONException) {
            showInvalidRequest()
        }
    }

    private fun handleFilesJson(json: String, hostname: String) {
        try {
            val jsonObject = JSONObject(json)
            val filesJsonArray = jsonObject.getJSONArray("files")

            val total = filesJsonArray.length()
            val dataSet = ArrayList<Attachment>(total)
            (0 until total).mapTo(dataSet) {
                val fileJsonObject = filesJsonArray.getJSONObject(it)

                val fileLink = UrlHelper.getAttachmentLink(hostname, fileJsonObject.optString("_id"), fileJsonObject.optString("name"))

                val attachmentTitle = AttachmentTitle.builder()
                        .setTitle(fileJsonObject.optString("name"))
                        .setLink(fileLink)
                        .setDownloadLink(fileLink)
                        .build()

                val attachment = Attachment.builder()

                val type = fileJsonObject.optString("type")
                when {
                    type.startsWith("image") -> attachment.setImageUrl(fileLink)
                    type.startsWith("audio") -> attachment.setAudioUrl(fileLink)
                    type.startsWith("video") -> attachment.setVideoUrl(fileLink)
                }

                attachment.setCollapsed(false)
                        .setAttachmentTitle(attachmentTitle)
                        .setTimestamp(getSafeTimestamp(fileJsonObject.optString("uploadedAt")))
                        .build()
            }

            if (dataSet.isEmpty() && !hasItem) {
                showEmptyViewMessage(context.getString(R.string.fragment_room_list_no_file_list_to_show))
            } else {
                if (dataSet.isNotEmpty()) {
                    hasItem = true
                    showFileList(dataSet, jsonObject.optString("total"))
                }
            }
        } catch (exception: JSONException) {
            showInvalidRequest()
        }
    }

    private fun handleMembersJson(json: String) {
        try {
            val jsonObject = JSONObject(json)
            val membersJsonArray = jsonObject.getJSONArray("members")

            val total = membersJsonArray.length()
            val dataSet = ArrayList<User>(total)
            (0 until total).mapTo(dataSet) {
                getUserFromJsonObject(membersJsonArray.getJSONObject(it))
            }

            if (dataSet.isEmpty() && !hasItem) {
                showEmptyViewMessage(context.getString(R.string.fragment_room_list_no_member_list_to_show))
            } else {
                if (dataSet.isNotEmpty()) {
                    hasItem = true
                    showMemberList(dataSet, jsonObject.optString("total"))
                }
            }
        } catch (exception: JSONException) {
            showInvalidRequest()
        }
    }

    private fun getUserFromJsonObject(jsonObject: JSONObject): User {
        return User.builder()
                .setId(jsonObject.optString("_id"))
                .setName(jsonObject.optString("name"))
                .setUsername(jsonObject.optString("username"))
                .setStatus(jsonObject.optString("status"))
                .setUtcOffset(jsonObject.optLong("utcOffset").toDouble())
                .build()
    }

    private fun getLongTimestamp(timestamp: String): Long {
        return if (timestamp.isNotBlank()) {
            Timestamp.valueOf(getSafeTimestamp(timestamp)).time
        } else {
            0
        }
    }

    private fun getSafeTimestamp(timestamp: String): String =
            timestamp.replace("T", " ").replace("Z", "")

    private fun showPinnedMessageList(dataSet: ArrayList<Message>, total: String) {
        mainHandler.post {
            view.showWaitingView(false)
            view.showPinnedMessages(dataSet, total)
        }
    }

    private fun showFavoriteMessageList(dataSet: ArrayList<Message>, total: String) {
        mainHandler.post {
            view.showWaitingView(false)
            view.showFavoriteMessages(dataSet, total)
        }
    }

    private fun showFileList(dataSet: ArrayList<Attachment>, total: String) {
        mainHandler.post {
            view.showWaitingView(false)
            view.showFileList(dataSet, total)
        }
    }

    private fun showMemberList(dataSet: ArrayList<User>, total: String) {
        mainHandler.post {
            view.showWaitingView(false)
            view.showMemberList(dataSet, total)
        }
    }

    private fun showInvalidRequest() {
        mainHandler.post {
            view.showWaitingView(false)
            view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, context.getString(R.string.make_sure_your_server_version_is_up_to_date)))
        }
    }

    private fun showEmptyViewMessage(message: String) {
        mainHandler.post {
            view.showWaitingView(false)
            view.showMessage(message)
        }
    }

    private fun showErrorMessage(message: String) {
        mainHandler.post {
            view.showWaitingView(false)
            view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, message))
        }
    }

    private val mainHandler = Handler(context.mainLooper)
    private var hasItem: Boolean = false
}