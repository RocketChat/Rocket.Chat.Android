package chat.rocket.android.fragment.chatroom.list

import android.content.Context
import android.os.Handler
import android.util.Log
import chat.rocket.android.R
import chat.rocket.android.api.rest.RestApiHelper
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.core.SyncState
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

    // TODO (after the REST api fixes)
    override fun requestFileList(roomId: String,
                                 roomType: String,
                                 hostname: String,
                                 token: String,
                                 userId: String,
                                 offset: Int) {}

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

                val timestampString = messageJsonObject.optString("ts")
                val timestamp = if (timestampString.isBlank()) {
                    0
                } else {
                    Timestamp.valueOf(timestampString.replace("T", " ").replace("Z", "")).time
                }

                val editedAtString = messageJsonObject.optString("_updatedAt")
                val editedAt = if (editedAtString.isBlank()) {
                    0
                } else {
                    Timestamp.valueOf(editedAtString.replace("T", " ").replace("Z", "")).time
                }

                Message.builder()
                        .setId(messageJsonObject.optString("_id"))
                        .setRoomId(messageJsonObject.optString("rid"))
                        .setMessage(messageJsonObject.optString("msg"))
                        .setUser(getUserFromJsonObject(userJsonObject))
                        .setTimestamp(timestamp)
                        .setEditedAt(editedAt)
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
        }catch (exception: JSONException) {
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