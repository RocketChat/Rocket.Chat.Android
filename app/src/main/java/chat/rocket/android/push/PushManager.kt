package chat.rocket.android.push

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import android.text.Html
import android.text.Spanned
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.siteName
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.common.model.RoomType
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.runBlocking
import se.ansman.kotshi.JsonSerializable
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Refer to: https://github.com/RocketChat/Rocket.Chat.Android/blob/9e846b7fde8fe0c74b9e0117c37ce49293308db5/app/src/main/java/chat/rocket/android/push/PushManager.kt
 * for old source code.
 */
class PushManager @Inject constructor(
    private val groupedPushes: GroupedPush,
    private val manager: NotificationManager,
    private val moshi: Moshi,
    private val getAccountInteractor: GetAccountInteractor,
    private val getSettingsInteractor: GetSettingsInteractor,
    private val context: Context
) {
    private val randomizer = Random()

    /**
     * Handles a receiving push by creating and displaying an appropriate notification based
     * on the *data* param bundle received.
     */
    @Synchronized
    fun handle(data: Bundle) = runBlocking {
        val message = data["message"] as String?
        val ejson = data["ejson"] as String?
        val title = data["title"] as String?
        val notId = data["notId"] as String? ?: randomizer.nextInt().toString()
        val image = data["image"] as String?
        val style = data["style"] as String?
        val summaryText = data["summaryText"] as String?
        val count = data["count"] as String?

        try {
            val adapter = moshi.adapter<PushInfo>(PushInfo::class.java)
            val info = adapter.fromJson(ejson)

            val pushMessage = PushMessage(title!!, message!!, info!!, image, count, notId, summaryText, style)

            Timber.d("Received push message: $pushMessage")

            showNotification(pushMessage)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing PUSH message: $data")
            ex.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    private suspend fun showNotification(pushMessage: PushMessage) {
        if (!hasAccount(pushMessage.info.host)) {
            Timber.d("ignoring push message: $pushMessage")
            return
        }

        val notId = pushMessage.notificationId.toInt()
        val host = pushMessage.info.host
        val groupTuple = getGroupForHost(host)

        groupTuple.second.incrementAndGet()
        val notIdListForHostname: MutableList<PushMessage>? = groupedPushes.hostToPushMessageList.get(host)
        if (notIdListForHostname == null) {
            groupedPushes.hostToPushMessageList[host] = arrayListOf(pushMessage)
        } else {
            notIdListForHostname.add(0, pushMessage)
        }

        val notification = createSingleNotification(pushMessage)
        val pushMessageList = groupedPushes.hostToPushMessageList[host]

        notification?.let {
            manager.notify(notId, notification)
        }

        pushMessageList?.let {
            if (pushMessageList.size > 1) {
                val groupNotification = createGroupNotification(pushMessage)
                groupNotification?.let {
                    NotificationManagerCompat.from(context).notify(groupTuple.first, groupNotification)
                }
            }
        }
    }

    private fun getGroupForHost(host: String): TupleGroupIdMessageCount {
        val size = groupedPushes.groupMap.size
        var group = groupedPushes.groupMap[host]
        if (group == null) {
            group = TupleGroupIdMessageCount(size + 1, AtomicInteger(0))
            groupedPushes.groupMap[host] = group
        }
        return group
    }

    private suspend fun hasAccount(host: String): Boolean {
        return getAccountInteractor.get(host) != null
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun createGroupNotification(pushMessage: PushMessage): Notification? {
        with(pushMessage) {
            val host = info.host

            val builder = createBaseNotificationBuilder(pushMessage, grouped = true)
                    .setGroupSummary(true)

            if (style == null || style == "inbox") {
                val pushMessageList = groupedPushes.hostToPushMessageList[host]

                pushMessageList?.let {
                    val count = pushMessageList.filter {
                        it.title == title
                    }.size

                    builder.setContentTitle(getTitle(count, title))

                    val inbox = NotificationCompat.InboxStyle()
                            .setBigContentTitle(getTitle(count, title))

                    for (push in pushMessageList) {
                        inbox.addLine(push.message)
                    }

                    builder.setStyle(inbox)
                }
            } else {
                val bigText = NotificationCompat.BigTextStyle()
                        .bigText(message.fromHtml())
                        .setBigContentTitle(title.fromHtml())

                builder.setStyle(bigText)
            }

            return builder.build()
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun createSingleNotification(pushMessage: PushMessage): Notification? {
        with(pushMessage) {
            val host = info.host

            val builder = createBaseNotificationBuilder(pushMessage)
                    .setGroupSummary(false)

            if (style == null || "inbox" == style) {
                val pushMessageList = groupedPushes.hostToPushMessageList.get(host)

                pushMessageList?.let {
                    val userMessages = pushMessageList.filter {
                        it.notificationId == pushMessage.notificationId
                    }

                    val count = pushMessageList.filter {
                        it.title == title
                    }.size

                    builder.setContentTitle(getTitle(count, title))

                    if (count > 1) {
                        val inbox = NotificationCompat.InboxStyle()
                        inbox.setBigContentTitle(getTitle(count, title))
                        for (push in userMessages) {
                            inbox.addLine(push.message)
                        }

                        builder.setStyle(inbox)
                    } else {
                        val bigTextStyle = NotificationCompat.BigTextStyle()
                                .bigText(message.fromHtml())
                        builder.setStyle(bigTextStyle)
                    }
                }
            } else {
                val bigTextStyle = NotificationCompat.BigTextStyle()
                        .bigText(message.fromHtml())
                builder.setStyle(bigTextStyle)
            }

            return builder.build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBaseNotificationBuilder(pushMessage: PushMessage, grouped: Boolean = false): NotificationCompat.Builder {
        return with(pushMessage) {
            val id = notificationId.toInt()
            val host = info.host
            val contentIntent = getContentIntent(context, id, pushMessage, grouped)
            val deleteIntent = getDismissIntent(context, pushMessage)

            val builder = NotificationCompat.Builder(context, host)
                    .setWhen(info.createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setGroup(host)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)
                    .setMessageNotification()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(host, host, NotificationManager.IMPORTANCE_HIGH)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.enableLights(false)
                channel.enableVibration(true)
                channel.setShowBadge(true)
                manager.createNotificationChannel(channel)
            }

            //TODO: Get Site_Name PublicSetting from cache
            val subText = getSiteName(host)
            if (subText.isNotEmpty()) {
                builder.setSubText(subText)
            }

            return@with builder
        }
    }

    private fun getSiteName(host: String): String {
        val settings = getSettingsInteractor.get(host)
        return settings.siteName() ?: "Rocket.Chat"
    }

    private fun getTitle(messageCount: Int, title: String): CharSequence {
        return if (messageCount > 1) "($messageCount) ${title.fromHtml()}" else title.fromHtml()
    }

    private fun getDismissIntent(context: Context, pushMessage: PushMessage): PendingIntent {
        val deleteIntent = Intent(context, DeleteReceiver::class.java)
                .putExtra(EXTRA_NOT_ID, pushMessage.notificationId.toInt())
                .putExtra(EXTRA_HOSTNAME, pushMessage.info.host)
        return PendingIntent.getBroadcast(context, pushMessage.notificationId.toInt(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getContentIntent(context: Context, notificationId: Int, pushMessage: PushMessage, grouped: Boolean = false): PendingIntent {
        val notificationIntent = context.changeServerIntent(pushMessage.info.host)
        // TODO - add support to go directly to the chatroom
        /*if (!grouped) {
            notificationIntent.putExtra(EXTRA_ROOM_ID, pushMessage.info.roomId)
        }*/
        return PendingIntent.getActivity(context, randomizer.nextInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // CharSequence extensions
    private fun CharSequence.fromHtml(): Spanned {
        return Html.fromHtml(this as String)
    }

    //Notification.Builder extensions
    @RequiresApi(Build.VERSION_CODES.N)
    private fun Notification.Builder.addReplyAction(pushMessage: PushMessage): Notification.Builder {
        val replyRemoteInput = android.app.RemoteInput.Builder(REMOTE_INPUT_REPLY)
                .setLabel(REPLY_LABEL)
                .build()
        //TODO: Implement this when we have sendMessage call
//        val replyIntent = Intent(context, ReplyReceiver::class.java)
//        replyIntent.putExtra(EXTRA_PUSH_MESSAGE, pushMessage as Serializable)
//        val pendingIntent = PendingIntent.getBroadcast(
//                context, randomizer.nextInt(), replyIntent, 0)
//        val replyAction =
//                Notification.Action.Builder(
//                        Icon.createWithResource(context, R.drawable.ic_reply), REPLY_LABEL, pendingIntent)
//                        .addRemoteInput(replyRemoteInput)
//                        .setAllowGeneratedReplies(true)
//                        .build()
//        this.addAction(replyAction)
        return this
    }

    // NotificationCompat.Builder extensions
    private fun NotificationCompat.Builder.addReplyAction(pushMessage: PushMessage): NotificationCompat.Builder {
        val replyRemoteInput = RemoteInput.Builder(REMOTE_INPUT_REPLY)
                .setLabel(REPLY_LABEL)
                .build()
        //TODO: Implement when we have sendMessage call
//        val replyIntent = Intent(context, ReplyReceiver::class.java)
//        replyIntent.putExtra(EXTRA_PUSH_MESSAGE, pushMessage as Serializable)
//        val pendingIntent = PendingIntent.getBroadcast(
//                context, randomizer.nextInt(), replyIntent, 0)
//        val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_reply, REPLY_LABEL, pendingIntent)
//                .addRemoteInput(replyRemoteInput)
//                .setAllowGeneratedReplies(true)
//                .build()
//
//        this.addAction(replyAction)
        return this
    }

    private fun NotificationCompat.Builder.setMessageNotification(): NotificationCompat.Builder {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val res = context.resources
        val smallIcon = res.getIdentifier(
                "rocket_chat_notification", "drawable", context.packageName)
        with(this, {
            setAutoCancel(true)
            setShowWhen(true)
            color = context.resources.getColor(R.color.colorPrimary)
            setDefaults(Notification.DEFAULT_ALL)
            setSmallIcon(smallIcon)
            setSound(alarmSound)
        })
        return this
    }
}

data class PushMessage(
    val title: String,
    val message: String,
    val info: PushInfo,
    val image: String? = null,
    val count: String? = null,
    val notificationId: String,
    val summaryText: String? = null,
    val style: String? = null
)

@JsonSerializable
data class PushInfo(
    @Json(name = "host") val hostname: String,
    @Json(name = "rid") val roomId: String,
    val type: RoomType,
    val name: String?,
    val sender: PushSender?
) {
    val createdAt: Long
        get() = System.currentTimeMillis()
    val host by lazy {
        sanitizeUrl(hostname)
    }

    private fun sanitizeUrl(baseUrl: String): String {
        var url = baseUrl.trim()
        while (url.endsWith('/')) {
            url = url.dropLast(1)
        }

        return url
    }
}

@JsonSerializable
data class PushSender(
    @Json(name = "_id") val id: String,
    val username: String?,
    val name: String?
)

const val EXTRA_NOT_ID = "chat.rocket.android.EXTRA_NOT_ID"
const val EXTRA_HOSTNAME = "chat.rocket.android.EXTRA_HOSTNAME"
const val EXTRA_PUSH_MESSAGE = "chat.rocket.android.EXTRA_PUSH_MESSAGE"
const val EXTRA_ROOM_ID = "chat.rocket.android.EXTRA_ROOM_ID"
private const val REPLY_LABEL = "REPLY"
private const val REMOTE_INPUT_REPLY = "REMOTE_INPUT_REPLY"
