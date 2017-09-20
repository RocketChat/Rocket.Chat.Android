package chat.rocket.android.fragment.chatroom.list

import android.content.Context
import android.os.Handler
import android.util.Log
import chat.rocket.android.R
import chat.rocket.android.api.rest.RestApiHelper
import chat.rocket.android.helper.OkHttpHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.core.SyncState
import chat.rocket.core.models.Message
import chat.rocket.core.models.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp

class RoomListPresenter(val context: Context, val view: RoomListContract.View): RoomListContract.Presenter {
    val mainHandler = Handler(context.mainLooper)

    override fun requestPinnedMessages(roomId: String,
                                       roomType: String,
                                       hostname: String,
                                       token: String,
                                       userId: String) {

        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForPinnedMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, e.message)) }
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
                                        // Note: There is no result to UTC OFFSET but as it is a required attribute to the user we can set it as 0.
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
                            mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }

    override fun requestFavoriteMessages(roomId: String,
                                         roomType: String,
                                         hostname: String,
                                         token: String,
                                         userId: String) {
        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForFavoriteMessages(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, e.message)) }
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
                                        // Note: There is no result to UTC OFFSET but as it is a required attribute to the user we can set it as 0.
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
                            mainHandler.post { view.showFavoriteMessages(dataSet) }
                        } else {
                            mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }

    // TODO (need a rest api fix)
    override fun requestFileList(roomId: String,
                                 roomType: String,
                                 hostname: String,
                                 token: String,
                                 userId: String) {
//        OkHttpHelper.getClient()
//                .newCall(RestApiHelper.getRequestForFileList(roomId,
//                        roomType,
//                        hostname,
//                        token,
//                        userId))
//                .enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, e.message)) }
//                    }
//
//                    @Throws(IOException::class)
//                    override fun onResponse(call: Call, response: Response) {
//                        if (response.isSuccessful) {
//                            val jSONObject = JSONObject(response.body()?.string())
//                            val filesJSONArray = jSONObject.get("files") as JSONArray
//
//                            val filesJSONArrayLength = filesJSONArray.length()
//                            val amazonS3JSONArray = JSONArray()
//                            for (i in 0 until filesJSONArrayLength) {
//                                amazonS3JSONArray.put(filesJSONArray.getJSONObject(i).get("AmazonS3"))
//                            }
//
//                            val pathJSONArray = JSONArray()
//                            val amazonS3JSONArrayLength = amazonS3JSONArray.length()
//                            for (i in 0 until amazonS3JSONArrayLength) {
//                                pathJSONArray.put(amazonS3JSONArray.getJSONObject(i).get("path"))
//                            }
//                            val pathJSONArrayLength = pathJSONArray.length()
//                            val dataSet = ArrayList<String>(pathJSONArrayLength)
//                            (0 until pathJSONArrayLength).mapTo(dataSet) {
//                                UrlHelper.getUrlForFile(pathJSONArray.get(it).toString(), userId, token)
//                            }
//
//                            mainHandler.post { view.showFileList(dataSet) }
//                        } else {
//                            mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, response.message())) }
//                        }
//                    }
//                })
    }

    override fun requestMemberList(roomId: String,
                                   roomType: String,
                                   hostname: String,
                                   token: String,
                                   userId: String) {
        OkHttpHelper.getClient()
                .newCall(RestApiHelper.getRequestForMemberList(roomId,
                        roomType,
                        hostname,
                        token,
                        userId))
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, e.message)) }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val jSONObject = JSONObject(response.body()?.string())
                            Log.i("json", "= " + jSONObject)
                            Log.i("json", "total = " + jSONObject.get("total"))
                            val membersJSONArray = jSONObject.get("members") as JSONArray

                            val membersJSONArrayLength = membersJSONArray.length()
                            val dataSet = ArrayList<User>(membersJSONArrayLength)
                            (0 until membersJSONArrayLength).mapTo(dataSet) {
                                User.builder()
                                        .setId(membersJSONArray.getJSONObject(it).optString("_id"))
                                        .setName(membersJSONArray.getJSONObject(it).optString("name"))
                                        .setUsername(membersJSONArray.getJSONObject(it).optString("username"))
                                        .setStatus(membersJSONArray.getJSONObject(it).optString("status"))
                                        .setUtcOffset(membersJSONArray.getJSONObject(it).optLong("utcOffset").toDouble())
                                        .build()

                            }
                            mainHandler.post { view.showMemberList(dataSet, jSONObject.get("total").toString()) }
                        } else {
                            mainHandler.post { view.showMessage(context.getString(R.string.fragment_room_list_could_not_load_your_request, response.message())) }
                        }
                    }
                })
    }
}
