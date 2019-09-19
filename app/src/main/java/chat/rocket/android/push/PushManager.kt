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
import android.text.Html
import android.text.Spanned
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import chat.rocket.android.R
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.siteName
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class PushManager @Inject constructor(
    private val groupedPushes: GroupedPush,
    private val notificationManager: NotificationManager,
    private val moshi: Moshi,
    private val getAccountInteractor: GetAccountInteractor,
    private val getSettingsInteractor: GetSettingsInteractor,
    private val context: Context,
    private val serverInteractor: GetCurrentServerInteractor,
    private val factory: RocketChatClientFactory
) {
    private val random = Random()

    fun registerPushNotificationToken(token: String) = GlobalScope.launch(Dispatchers.IO) {
        serverInteractor.get()?.let { currentServer ->
            registerPushNotificationToken(factory.get(currentServer), token)
        }
    }

    /**
     * Handles a receiving push by creating and displaying an appropriate notification based
     * on the *data* param bundle received.
     */
    fun handle(data: Bundle) = runBlocking {
        val message = data["message"] as String?
        val ejson = data["ejson"] as String?
        val title = data["title"] as String?
        val notId = data["notId"] as String? ?: random.nextInt().toString()
        val image = data["image"] as String?
        val style = data["style"] as String?
        val summaryText = data["summaryText"] as String?
        val count = data["count"] as String?

        try {
            if (title != null && message != null) {
                val pushInfo = if (ejson != null) {
                    moshi.adapter<PushInfo>(PushInfo::class.java).fromJson(ejson)
                } else {
                    null
                }
                val pushMessage = PushMessage(
                    title = title,
                    message = message,
                    info = pushInfo ?: PushInfo(
                        hostname = "",
                        roomId = "",
                        type = roomTypeOf(RoomType.CHANNEL),
                        name = "",
                        sender = null
                    ),
                    image = image,
                    count = count,
                    notificationId = notId,
                    summaryText = summaryText,
                    style = style
                )
                showNotification(pushMessage)
                Timber.d("Received push message: $pushMessage")
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Error parsing push message: $data")
        }
    }

    @SuppressLint("NewApi")
    fun showNotification(pushMessage: PushMessage) {
        val notId = pushMessage.notificationId.toInt()
        val host = pushMessage.info.host

        createSingleNotification(pushMessage)?.let { notification ->
            NotificationManagerCompat.from(context).notify(notId, notification)
            if (!hasAccount(host)) {
                Timber.d("Test or generic Push message: $pushMessage sent")
                return
            }
        }


        val groupTuple = getGroupForHost(host)
        groupTuple.second.incrementAndGet()
        val notIdListForHostname: MutableList<PushMessage>? =
            groupedPushes.hostToPushMessageList[host]
        if (notIdListForHostname == null) {
            groupedPushes.hostToPushMessageList[host] = arrayListOf(pushMessage)
        } else {
            notIdListForHostname.add(0, pushMessage)
        }

        val pushMessageList = groupedPushes.hostToPushMessageList[host]
        pushMessageList?.let {
            if (pushMessageList.size > 1) {
                val groupNotification = createGroupNotification(pushMessage)
                groupNotification?.let {
                    NotificationManagerCompat.from(context)
                        .notify(groupTuple.first, groupNotification)
                }
            }
        }
    }

    private fun hasAccount(host: String): Boolean {
        return getAccountInteractor.get(host) != null
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

            val builder = createBaseNotificationBuilder(pushMessage).setGroupSummary(false)

            if (style == null || "inbox" == style) {
                val pushMessageList = groupedPushes.hostToPushMessageList[host]

                if (pushMessageList != null) {
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
                } else {
                    // We don't know which kind of push is this - maybe a test push, so just show it
                    val bigTextStyle = NotificationCompat.BigTextStyle()
                        .bigText(message.fromHtml())
                    builder.setStyle(bigTextStyle)
                    return builder.build()
                }
            } else {
                val bigTextStyle = NotificationCompat.BigTextStyle().bigText(message.fromHtml())
                builder.setStyle(bigTextStyle)
            }

            return builder.build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBaseNotificationBuilder(
        pushMessage: PushMessage,
        grouped: Boolean = false
    ): NotificationCompat.Builder {
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

            if (host.isEmpty()) {
                builder.setContentIntent(deleteIntent)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId: String
                val channelName: String
                if (host.isEmpty()) {
                    channelName = "Test Notification"
                    channelId = "test-channel"
                } else {
                    channelName = host
                    channelId = host
                }
                val channel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.enableLights(false)
                channel.enableVibration(true)
                channel.setShowBadge(true)
                notificationManager.createNotificationChannel(channel)
                builder.setChannelId(channelId)
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
        return PendingIntent.getBroadcast(
            context,
            pushMessage.notificationId.toInt(),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getContentIntent(
        context: Context,
        notificationId: Int,
        pushMessage: PushMessage,
        grouped: Boolean = false
    ): PendingIntent {
        val roomId = if (!grouped) pushMessage.info.roomId else null
        val notificationIntent =
            context.changeServerIntent(pushMessage.info.host, chatRoomId = roomId)
        return PendingIntent.getActivity(
            context,
            random.nextInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // CharSequence extensions
    private fun CharSequence.fromHtml(): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this as String, FROM_HTML_MODE_LEGACY, null, null)
        } else {
            Html.fromHtml(this as String)
        }
    }

    private fun NotificationCompat.Builder.setMessageNotification(): NotificationCompat.Builder {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val res = context.resources
        val smallIcon = res.getIdentifier(
            "rocket_chat_notification", "drawable", context.packageName
        )
        with(this) {
            setAutoCancel(true)
            setShowWhen(true)
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            setDefaults(Notification.DEFAULT_ALL)
            setSmallIcon(smallIcon)
            setSound(alarmSound)
        }
        return this
    }
}

const val EXTRA_NOT_ID = "chat.rocket.android.EXTRA_NOT_ID"
const val EXTRA_HOSTNAME = "chat.rocket.android.EXTRA_HOSTNAME"