package chat.rocket.android.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import android.text.Html
import android.text.Spanned
import android.util.Log
import chat.rocket.android.BackgroundLooper
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.RocketChatCache
import chat.rocket.android.activity.MainActivity
import chat.rocket.android.helper.Logger
import chat.rocket.core.interactors.MessageInteractor
import chat.rocket.core.models.Room
import chat.rocket.core.models.User
import chat.rocket.persistence.realm.repositories.RealmMessageRepository
import chat.rocket.persistence.realm.repositories.RealmRoomRepository
import chat.rocket.persistence.realm.repositories.RealmUserRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import okhttp3.HttpUrl
import org.json.JSONObject
import java.io.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

typealias TupleRoomUser = Pair<Room, User>
typealias TupleGroupIdMessageCount = Pair<Int, AtomicInteger>

object PushManager {
    const val EXTRA_NOT_ID = "chat.rocket.android.EXTRA_NOT_ID"
    const val EXTRA_HOSTNAME = "chat.rocket.android.EXTRA_HOSTNAME"
    const val EXTRA_PUSH_MESSAGE = "chat.rocket.android.EXTRA_PUSH_MESSAGE"
    const val EXTRA_ROOM_ID = "chat.rocket.android.EXTRA_ROOM_ID"
    private const val REPLY_LABEL = "REPLY"
    private const val REMOTE_INPUT_REPLY = "REMOTE_INPUT_REPLY"

    // Notifications received from the same server are grouped in a single bundled notification.
    // This map associates a host to a group id.
    private val groupMap = HashMap<String, TupleGroupIdMessageCount>()

    // Map a hostname to a list of push messages that pertain to it.
    private val hostToPushMessageList = HashMap<String, MutableList<PushMessage>>()
    private val randomizer = Random()

    /**
     * Handles a receiving push by creating and displaying an appropriate notification based
     * on the *data* param bundle received.
     */
    @Synchronized
    fun handle(context: Context, data: Bundle) {
        val appContext = context.applicationContext
        val message = data["message"] as String?
        val image = data["image"] as String?
        val ejson = data["ejson"] as String?
        val notId = data["notId"] as String? ?: randomizer.nextInt().toString()
        val style = data["style"] as String?
        val summaryText = data["summaryText"] as String?
        val count = data["count"] as String?
        val title = data["title"] as String?

        if (ejson == null || message == null || title == null) {
            return
        }

        val lastPushMessage = PushMessage(title, message, image, ejson, count, notId, summaryText, style)

        // We should use Timber here
        if (BuildConfig.DEBUG) {
            Log.d(PushMessage::class.java.simpleName, lastPushMessage.toString())
        }

        showNotification(appContext, lastPushMessage)
    }

    /**
     * Clear all messages received to a given host the user is signed-in.
     */
    fun clearNotificationsByHost(host: String) {
        hostToPushMessageList.remove(host)
    }

    /**
     * Remove a notification solely by it's unique id.
     */
    fun clearNotificationsByNotificationId(notificationId: Int) {
        if (hostToPushMessageList.isNotEmpty()) {
            for (entry in hostToPushMessageList.entries) {
                entry.value.removeAll {
                    it.notificationId.toInt() == notificationId
                }
            }
        }
    }

    /**
     * Clear notifications by the host they belong to and its unique id.
     */
    fun clearNotificationsByHostAndNotificationId(host: String?, notificationId: Int?) {
        if (host == null || notificationId == null) {
            return
        }
        if (hostToPushMessageList.isNotEmpty()) {
            val notifications = hostToPushMessageList[host]
            notifications?.let {
                notifications.removeAll {
                    it.notificationId.toInt() == notificationId
                }
            }
        }
    }

    private fun isAndroidVersionAtLeast(minVersion: Int) = Build.VERSION.SDK_INT >= minVersion

    private fun getGroupForHost(host: String): TupleGroupIdMessageCount {
        val size = groupMap.size
        var group = groupMap.get(host)
        if (group == null) {
            group = TupleGroupIdMessageCount(size + 1, AtomicInteger(0))
            groupMap.put(host, group)
        }
        return group
    }

    internal fun showNotification(context: Context, lastPushMessage: PushMessage) {
        if (lastPushMessage.host == null || lastPushMessage.message == null || lastPushMessage.title == null) {
            return
        }
        val manager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notId = lastPushMessage.notificationId.toInt()
        val host = lastPushMessage.host
        val groupTuple = getGroupForHost(host)

        groupTuple.second.incrementAndGet()
        val notIdListForHostname: MutableList<PushMessage>? = hostToPushMessageList.get(host)
        if (notIdListForHostname == null) {
            hostToPushMessageList.put(host, arrayListOf(lastPushMessage))
        } else {
            notIdListForHostname.add(0, lastPushMessage)
        }
        if (isAndroidVersionAtLeast(Build.VERSION_CODES.N)) {
            val notification = createSingleNotificationForNougatAndAbove(context, lastPushMessage)
            val groupNotification = createGroupNotificationForNougatAndAbove(context, lastPushMessage)
            notification?.let {
                manager.notify(notId, notification)
            }

            groupNotification?.let {
                manager.notify(groupTuple.first, groupNotification)
            }
        } else {
            val notification = createSingleNotification(context, lastPushMessage)
            val pushMessageList = hostToPushMessageList.get(host)

            notification?.let {
                NotificationManagerCompat.from(context).notify(notId, notification)
            }

            pushMessageList?.let {
                if (pushMessageList.size > 1) {
                    val groupNotification = createGroupNotification(context, lastPushMessage)
                    groupNotification?.let {
                        NotificationManagerCompat.from(context).notify(groupTuple.first, groupNotification)
                    }
                }
            }
        }
    }

    internal fun createGroupNotification(context: Context, lastPushMessage: PushMessage): Notification? {
        with(lastPushMessage) {
            if (host == null || message == null || title == null) {
                return null
            }
            val id = lastPushMessage.notificationId.toInt()
            val contentIntent = getContentIntent(context, id, lastPushMessage)
            val deleteIntent = getDismissIntent(context, lastPushMessage)
            val builder = NotificationCompat.Builder(context)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setGroup(host)
                    .setGroupSummary(true)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deleteIntent)
                    .setMessageNotification()

            val subText = RocketChatCache(context).getHostSiteName(host)
            if (subText.isNotEmpty()) {
                builder.setSubText(subText)
            }

            if (style == null || style == "inbox") {
                val pushMessageList = hostToPushMessageList.get(host)

                pushMessageList?.let {
                    val messageCount = pushMessageList.size
                    val summary = summaryText?.replace("%n%", messageCount.toString())
                            ?.fromHtml() ?: "$messageCount new messages"
                    builder.setNumber(messageCount)
                    if (messageCount > 1) {
                        val firstPush = pushMessageList[0]
                        val singleConversation = pushMessageList.filter {
                            firstPush.sender?.username != it.sender?.username
                        }.isEmpty()

                        val inbox = NotificationCompat.InboxStyle()
                                .setBigContentTitle(if (singleConversation) title else summary)

                        for (push in pushMessageList) {
                            if (singleConversation) {
                                inbox.addLine(push.message)
                            } else {
                                inbox.addLine("<font color='black'>${push.title}</font> <font color='gray'>${push.message}</font>".fromHtml())
                            }
                        }

                        builder.setStyle(inbox)
                    } else {
                        val firstMsg = pushMessageList[0]
                        if (firstMsg.host == null || firstMsg.message == null || firstMsg.title == null) {
                            return null
                        }
                        val bigText = NotificationCompat.BigTextStyle()
                                .bigText(firstMsg.message.fromHtml())
                                .setBigContentTitle(firstMsg.title.fromHtml())

                        builder.setStyle(bigText)
                    }
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

    @RequiresApi(Build.VERSION_CODES.N)
    internal fun createGroupNotificationForNougatAndAbove(context: Context, lastPushMessage: PushMessage): Notification? {
        with(lastPushMessage) {
            if (host == null || message == null || title == null) {
                return null
            }
            val manager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, lastPushMessage, grouped = true)
            val deleteIntent = getDismissIntent(context, lastPushMessage)

            val builder = Notification.Builder(context)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setGroup(host)
                    .setGroupSummary(true)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deleteIntent)
                    .setMessageNotification(context)

            if (isAndroidVersionAtLeast(Build.VERSION_CODES.O)) {
                builder.setChannelId(host)
                val groupChannel = NotificationChannel(host, host, NotificationManager.IMPORTANCE_HIGH)
                groupChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                groupChannel.enableLights(false)
                groupChannel.enableVibration(true)
                groupChannel.setShowBadge(true)
                manager.createNotificationChannel(groupChannel)
            }

            val subText = RocketChatCache(context).getHostSiteName(host)
            if (subText.isNotEmpty()) {
                builder.setSubText(subText)
            }

            if (style == null || style == "inbox") {
                val pushMessageList = hostToPushMessageList.get(host)

                pushMessageList?.let {
                    val count = pushMessageList.filter {
                        it.title == title
                    }.size

                    builder.setContentTitle(getTitle(count, title))

                    val inbox = Notification.InboxStyle()
                            .setBigContentTitle(getTitle(count, title))

                    for (push in pushMessageList) {
                        inbox.addLine(push.message)
                    }

                    builder.setStyle(inbox)
                }
            } else {
                val bigText = Notification.BigTextStyle()
                        .bigText(message.fromHtml())
                        .setBigContentTitle(title.fromHtml())

                builder.setStyle(bigText)
            }

            return builder.build()
        }
    }

    internal fun createSingleNotification(context: Context, lastPushMessage: PushMessage): Notification? {
        with(lastPushMessage) {
            if (host == null || message == null || title == null) {
                return null
            }
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, lastPushMessage)
            val deleteIntent = getDismissIntent(context, lastPushMessage)

            val builder = NotificationCompat.Builder(context)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setGroupSummary(false)
                    .setGroup(host)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)
                    .setMessageNotification()

            val subText = RocketChatCache(context).getHostSiteName(host)
            if (subText.isNotEmpty()) {
                builder.setSubText(subText)
            }

            val pushMessageList = hostToPushMessageList.get(host)

            pushMessageList?.let {
                val lastPushMsg = pushMessageList.last()
                if (lastPushMsg.host == null || lastPushMsg.message == null || lastPushMsg.title == null) {
                    return null
                }
                if (pushMessageList.isNotEmpty()) {
                    val messageCount = pushMessageList.size

                    val bigText = NotificationCompat.BigTextStyle()
                            .bigText(lastPushMsg.message.fromHtml())
                            .setBigContentTitle(lastPushMsg.title.fromHtml())
                    builder.setStyle(bigText).setNumber(messageCount)
                }
            }

            return builder.build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    internal fun createSingleNotificationForNougatAndAbove(context: Context, lastPushMessage: PushMessage): Notification? {
        val manager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        with(lastPushMessage) {
            if (host == null || message == null || title == null) {
                return null
            }
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, lastPushMessage)
            val deleteIntent = getDismissIntent(context, lastPushMessage)

            val builder = Notification.Builder(context)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setGroup(host)
                    .setGroupSummary(false)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)
                    .setMessageNotification(context)
                    .addReplyAction(context, lastPushMessage)

            if (isAndroidVersionAtLeast(android.os.Build.VERSION_CODES.O)) {
                builder.setChannelId(host)
                val channel = NotificationChannel(host, host, NotificationManager.IMPORTANCE_HIGH)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.enableLights(false)
                channel.enableVibration(true)
                channel.setShowBadge(true)
                manager.createNotificationChannel(channel)
            }

            val subText = RocketChatCache(context).getHostSiteName(host)
            if (subText.isNotEmpty()) {
                builder.setSubText(subText)
            }

            if (style == null || "inbox" == style) {
                val pushMessageList = hostToPushMessageList.get(host)

                pushMessageList?.let {
                    val userMessages = pushMessageList.filter {
                        it.notificationId == lastPushMessage.notificationId
                    }

                    val count = pushMessageList.filter {
                        it.title == title
                    }.size

                    builder.setContentTitle(getTitle(count, title))

                    if (count > 1) {
                        val inbox = Notification.InboxStyle()
                        inbox.setBigContentTitle(getTitle(count, title))
                        for (push in userMessages) {
                            inbox.addLine(push.message)
                        }

                        builder.setStyle(inbox)
                    } else {
                        val bigTextStyle = Notification.BigTextStyle()
                                .bigText(message.fromHtml())
                        builder.setStyle(bigTextStyle)
                    }
                }
            } else {
                val bigTextStyle = Notification.BigTextStyle()
                        .bigText(message.fromHtml())
                builder.setStyle(bigTextStyle)
            }

            return builder.build()
        }
    }

    private fun getTitle(messageCount: Int, title: String): CharSequence {
        return if (messageCount > 1) "($messageCount) ${title.fromHtml()}" else title.fromHtml()
    }

    private fun getDismissIntent(context: Context, pushMessage: PushMessage): PendingIntent {
        val deleteIntent = Intent(context, DeleteReceiver::class.java)
                .putExtra(EXTRA_NOT_ID, pushMessage.notificationId.toInt())
                .putExtra(EXTRA_HOSTNAME, pushMessage.host)
        return PendingIntent.getBroadcast(context, pushMessage.notificationId.toInt(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getContentIntent(context: Context, notificationId: Int, pushMessage: PushMessage, grouped: Boolean = false): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_NOT_ID, notificationId)
                .putExtra(EXTRA_HOSTNAME, pushMessage.host)
        if (!grouped) {
            notificationIntent.putExtra(EXTRA_ROOM_ID, pushMessage.rid)
        }
        return PendingIntent.getActivity(context, randomizer.nextInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // CharSequence extensions
    private fun CharSequence.fromHtml(): Spanned {
        return Html.fromHtml(this as String)
    }

    //Notification.Builder extensions
    @RequiresApi(Build.VERSION_CODES.N)
    private fun Notification.Builder.addReplyAction(ctx: Context, pushMessage: PushMessage): Notification.Builder {
        val replyRemoteInput = android.app.RemoteInput.Builder(REMOTE_INPUT_REPLY)
                .setLabel(REPLY_LABEL)
                .build()
        val replyIntent = Intent(ctx, ReplyReceiver::class.java)
        replyIntent.putExtra(EXTRA_PUSH_MESSAGE, pushMessage as Serializable)
        val pendingIntent = PendingIntent.getBroadcast(
                ctx, randomizer.nextInt(), replyIntent, 0)
        val replyAction =
                Notification.Action.Builder(
                        Icon.createWithResource(ctx, R.drawable.ic_reply), REPLY_LABEL, pendingIntent)
                        .addRemoteInput(replyRemoteInput)
                        .setAllowGeneratedReplies(true)
                        .build()
        this.addAction(replyAction)
        return this
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun Notification.Builder.setMessageNotification(ctx: Context): Notification.Builder {
        val res = ctx.resources
        val smallIcon = res.getIdentifier(
                "rocket_chat_notification", "drawable", ctx.packageName)
        with(this, {
            setAutoCancel(true)
            setShowWhen(true)
            setColor(res.getColor(R.color.colorRed400, ctx.theme))
            setSmallIcon(smallIcon)
        })
        return this
    }

    // NotificationCompat.Builder extensions
    private fun NotificationCompat.Builder.addReplyAction(pushMessage: PushMessage): NotificationCompat.Builder {
        val context = this.mContext
        val replyRemoteInput = RemoteInput.Builder(REMOTE_INPUT_REPLY)
                .setLabel(REPLY_LABEL)
                .build()
        val replyIntent = Intent(context, ReplyReceiver::class.java)
        replyIntent.putExtra(EXTRA_PUSH_MESSAGE, pushMessage as Serializable)
        val pendingIntent = PendingIntent.getBroadcast(
                context, randomizer.nextInt(), replyIntent, 0)
        val replyAction = NotificationCompat.Action.Builder(R.drawable.ic_reply, REPLY_LABEL, pendingIntent)
                .addRemoteInput(replyRemoteInput)
                .setAllowGeneratedReplies(true)
                .build()

        this.addAction(replyAction)
        return this
    }

    private fun NotificationCompat.Builder.setMessageNotification(): NotificationCompat.Builder {
        val ctx = this.mContext
        val res = ctx.resources
        val smallIcon = res.getIdentifier(
                "rocket_chat_notification", "drawable", ctx.packageName)
        with(this, {
            setAutoCancel(true)
            setShowWhen(true)
            color = ctx.resources.getColor(R.color.colorRed400)
            setDefaults(Notification.DEFAULT_ALL)
            setSmallIcon(smallIcon)
        })
        return this
    }

    internal data class PushMessage(
            val title: String? = null,
            val message: String? = null,
            val image: String? = null,
            val ejson: String? = null,
            val count: String? = null,
            val notificationId: String,
            val summaryText: String? = null,
            val style: String? = null) : Serializable {
        val host: String?
        val rid: String?
        val type: String?
        val channelName: String?
        val sender: Sender?
        val createdAt: Long

        init {
            val json = if (ejson == null) JSONObject() else JSONObject(ejson)
            host = json.optString("host", null)
            rid = json.optString("rid", null)
            type = json.optString("type", null)
            channelName = json.optString("name", null)
            val senderJson = json.optString("sender", null)
            if (senderJson != null && senderJson != "null") {
                sender = Sender(senderJson)
            } else {
                sender = null
            }
            createdAt = System.currentTimeMillis()
        }

        data class Sender(val sender: String) : Serializable {
            val _id: String?
            val username: String?
            val name: String?

            init {
                val json = JSONObject(sender)
                _id = json.optString("_id", null)
                username = json.optString("username", null)
                name = json.optString("name", null)
            }
        }
    }

    /**
     * BroadcastReceiver for dismissed notifications.
     */
    class DeleteReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notId = intent?.extras?.getInt(EXTRA_NOT_ID)
            val host = intent?.extras?.getString(EXTRA_HOSTNAME)
            if (host != null && notId != null) {
                clearNotificationsByHostAndNotificationId(host, notId)
            }
        }
    }

    /**
     * *EXPERIMENTAL*
     *
     * BroadcastReceiver for notifications' replies using Direct Reply feature (Android >= 7).
     */
    class ReplyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null) {
                return
            }

            synchronized(this) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val message: CharSequence? = extractMessage(intent)
                val pushMessage = intent?.extras?.getSerializable(EXTRA_PUSH_MESSAGE) as PushMessage?

                if (pushMessage?.host == null) {
                    return
                }

                pushMessage.let {
                    val groupTuple = groupMap.get(pushMessage.host)
                    val pushes = hostToPushMessageList.get(pushMessage.host)
                    pushes?.let {
                        val allMessagesFromSameUser = pushes.filter {
                            it.sender?._id == pushMessage.sender?._id
                        }
                        for (msg in allMessagesFromSameUser) {
                            manager.cancel(msg.notificationId.toInt())
                            groupTuple?.second?.decrementAndGet()
                        }

                        groupTuple?.let {
                            val groupNotId = groupTuple.first
                            val totalNot = groupTuple.second.get()
                            if (totalNot == 0) {
                                manager.cancel(groupNotId)
                            }
                        }
                        message?.let {
                            if (pushMessage.rid == null) {
                                return
                            }
                            val httpUrl = HttpUrl.parse(pushMessage.host)
                            httpUrl?.let {
                                val siteUrl = RocketChatCache(context).getSiteUrlFor(httpUrl.host())
                                if (siteUrl != null) {
                                    sendMessage(siteUrl, message, pushMessage.rid)
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun extractMessage(intent: Intent?): CharSequence? {
            val remoteInput: Bundle? =
                    RemoteInput.getResultsFromIntent(intent)
            return remoteInput?.getCharSequence(REMOTE_INPUT_REPLY)
        }

        // Just kept for reference. We should use this on rewrite with job schedulers
        private fun sendMessage(hostname: String, message: CharSequence, roomId: String) {
            val roomRepository = RealmRoomRepository(hostname)
            val userRepository = RealmUserRepository(hostname)
            val messageRepository = RealmMessageRepository(hostname)
            val messageInteractor = MessageInteractor(messageRepository, roomRepository)

            val singleRoom: Single<Room> = roomRepository.getById(roomId)
                    .filter({ it.isPresent })
                    .map({ it.get() })
                    .firstElement()
                    .toSingle()

            val singleUser: Single<User> = userRepository.getCurrent()
                    .filter({ it.isPresent })
                    .map({ it.get() })
                    .firstElement()
                    .toSingle()

            val roomUserTuple: Single<TupleRoomUser> = Single.zip(
                    singleRoom,
                    singleUser,
                    BiFunction { room, user -> TupleRoomUser(room, user) })

            roomUserTuple.flatMap { tuple -> messageInteractor.send(tuple.first, tuple.second, message as String) }
                    .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _ ->
                        // Empty
                    }, { throwable ->
                        throwable.printStackTrace()
                        Logger.report(throwable)
                    })
        }
    }
}