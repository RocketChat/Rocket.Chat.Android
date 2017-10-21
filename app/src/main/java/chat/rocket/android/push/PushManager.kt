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
import android.util.SparseArray
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
import org.json.JSONObject
import java.io.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

typealias TupleRoomUser = Pair<Room, User>
typealias TupleGroupIdMessageCount = Pair<Int, AtomicInteger>

object PushManager {
    const val REPLY_LABEL = "REPLY"
    const val REMOTE_INPUT_REPLY = "REMOTE_INPUT_REPLY"
    // Map associating a notification id to a list of corresponding messages ie. an id corresponds
    // to a user and the corresponding list is all the messages sent by him.
    private val messageStack = SparseArray<ArrayList<CharSequence>>()
    // Notifications received from the same server are grouped in a single bundled notification.
    // This map associates a host to a group id.
    private val groupMap = HashMap<String, TupleGroupIdMessageCount>()
    private val randomizer = Random()

    /**
     * Handles a receiving push by creating and displaying an appropriate notification based
     * on the *data* param bundle received.
     */
    @Synchronized
    fun handle(context: Context, data: Bundle) {
        val appContext = context.applicationContext
        val message = data["message"] as String
        val image = data["image"] as String
        val ejson = data["ejson"] as String
        val notId = data["notId"] as String
        val style = data["style"] as String
        val summaryText = data["summaryText"] as String
        val count = data["count"] as String
        val title = data["title"] as String
        val pushMessage = PushMessage(title, message, image, ejson, count, notId, summaryText, style)

        // We should use Timber here
        if (BuildConfig.DEBUG) {
            Log.d(PushMessage::class.java.simpleName, pushMessage.toString())
        }

        bundleMessage(notId.toInt(), pushMessage.message)

        showNotification(appContext, pushMessage)
    }

    /**
     * Clear all messages corresponding to a specific notification id (aka specific user)
     */
    fun clearMessageBundle(notificationId: Int) {
        messageStack.delete(notificationId)
    }

    private fun showNotification(context: Context, pushMessage: PushMessage) {
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notId = pushMessage.notificationId.toInt()

        val groupTuple = groupMap[pushMessage.host]
        val notification: Notification
        if (isAndroidVersionAtLeast(Build.VERSION_CODES.O)) {
            notification = createNotificationForOreoAndAbove(context, pushMessage)
            groupTuple?.let {
                val groupNotification = createOreoGroupNotification(context, pushMessage)
                notificationManager.notify(groupTuple.first, groupNotification)
                groupTuple.second.incrementAndGet()
            }
            notificationManager.notify(notId, notification)
        } else {
            notification = createCompatNotification(context, pushMessage)
            groupTuple?.let {
                val groupNotification = createCompatGroupNotification(context, pushMessage)
                NotificationManagerCompat.from(context).notify(groupTuple.first, groupNotification)
                groupTuple.second.incrementAndGet()
            }
            NotificationManagerCompat.from(context).notify(notId, notification)
        }
    }

    private fun isAndroidVersionAtLeast(minVersion: Int) = Build.VERSION.SDK_INT >= minVersion

    private fun bundleNotificationsToHost(host: String) {
        val size = groupMap.size
        groupMap.get(host)?.let {
            groupMap.put(host, TupleGroupIdMessageCount(size + 1, AtomicInteger(0)))
        }
    }

    private fun createCompatGroupNotification(context: Context, pushMessage: PushMessage): Notification {
        // Create notification group.
        bundleNotificationsToHost(pushMessage.host)
        val id = pushMessage.notificationId.toInt()
        val contentIntent = getContentIntent(context, id, pushMessage, group = true)
        val deleteIntent = getDismissIntent(context, id)
        val notGroupBuilder = NotificationCompat.Builder(context)
                .setWhen(pushMessage.createdAt)
                .setContentTitle(pushMessage.title.fromHtml())
                .setContentText(pushMessage.message.fromHtml())
                .setGroup(pushMessage.host)
                .setGroupSummary(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushMessage.message.fromHtml()))
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setMessageNotification()

        val subText = RocketChatCache(context).getHostSiteName(pushMessage.host)
        if (subText.isNotEmpty()) {
            notGroupBuilder.setSubText(subText)
        }

        val messages = messageStack.get(pushMessage.notificationId.toInt())
        val messageCount = messages.size

        if (messageCount > 1) {
            val summary = pushMessage.summaryText.replace("%n%", messageCount.toString())
            val inbox = NotificationCompat.InboxStyle()
                    .setBigContentTitle(pushMessage.title.fromHtml())
                    .setSummaryText(summary)

            notGroupBuilder.setStyle(inbox)
        } else {
            val bigText = NotificationCompat.BigTextStyle()
                    .bigText(pushMessage.message.fromHtml())
                    .setBigContentTitle(pushMessage.title.fromHtml())

            notGroupBuilder.setStyle(bigText)
        }

        return notGroupBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createOreoGroupNotification(context: Context, pushMessage: PushMessage): Notification {
        // Create notification group.
        bundleNotificationsToHost(pushMessage.host)
        val id = pushMessage.notificationId.toInt()
        val contentIntent = getContentIntent(context, id, pushMessage, group = true)
        val deleteIntent = getDismissIntent(context, id)
        val notGroupBuilder = Notification.Builder(context, pushMessage.notificationId)
                .setWhen(pushMessage.createdAt)
                .setChannelId(pushMessage.notificationId)
                .setContentTitle(pushMessage.title.fromHtml())
                .setContentText(pushMessage.message.fromHtml())
                .setGroup(pushMessage.host)
                .setGroupSummary(true)
                .setStyle(Notification.BigTextStyle().bigText(pushMessage.message.fromHtml()))
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setMessageNotification(context)

        val subText = RocketChatCache(context).getHostSiteName(pushMessage.host)
        if (subText.isNotEmpty()) {
            notGroupBuilder.setSubText(subText)
        }

        val messages = messageStack.get(pushMessage.notificationId.toInt())
        val messageCount = messages.size

        if (messageCount > 1) {
            val summary = pushMessage.summaryText.replace("%n%", messageCount.toString())
            val inbox = Notification.InboxStyle()
                    .setBigContentTitle(pushMessage.title.fromHtml())
                    .setSummaryText(summary)

            notGroupBuilder.setStyle(inbox)
        } else {
            val bigText = Notification.BigTextStyle()
                    .bigText(pushMessage.message.fromHtml())
                    .setBigContentTitle(pushMessage.title.fromHtml())

            notGroupBuilder.setStyle(bigText)
        }

        return notGroupBuilder.build()
    }

    private fun createCompatNotification(context: Context, pushMessage: PushMessage): Notification {
        with(pushMessage) {
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, pushMessage)
            val deleteIntent = getDismissIntent(context, id)

            val notificationBuilder = NotificationCompat.Builder(context)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setNumber(count.toInt())
                    .setGroup(host)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)
                    .setMessageNotification()
                    .addReplyAction(pushMessage)

            val subText = RocketChatCache(context).getHostSiteName(pushMessage.host)
            if (subText.isNotEmpty()) {
                notificationBuilder.setSubText(subText)
            }

            if ("inbox" == style) {
                val messages = messageStack.get(notificationId.toInt())
                val messageCount = messages.size
                if (messageCount > 1) {
                    val summary = summaryText.replace("%n%", messageCount.toString())
                            .fromHtml()
                    val inbox = NotificationCompat.InboxStyle()
                            .setBigContentTitle(title.fromHtml())
                            .setSummaryText(summary)

                    messages.forEach { msg ->
                        inbox.addLine(msg.fromHtml())
                    }

                    notificationBuilder.setStyle(inbox)
                } else {
                    val bigText = NotificationCompat.BigTextStyle()
                            .bigText(message.fromHtml())
                            .setBigContentTitle(title.fromHtml())

                    notificationBuilder.setStyle(bigText)
                }
            } else {
                notificationBuilder.setContentText(message.fromHtml())
            }

            return notificationBuilder.build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationForOreoAndAbove(context: Context, pushMessage: PushMessage): Notification {
        val manager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        with(pushMessage) {
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, pushMessage)
            val deleteIntent = getDismissIntent(context, id)

            val channel = NotificationChannel(notificationId, sender.username, NotificationManager.IMPORTANCE_HIGH)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.setShowBadge(true)
            manager.createNotificationChannel(channel)
            val notificationBuilder = Notification.Builder(context, notificationId)
                    .setWhen(createdAt)
                    .setChannelId(notificationId)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setNumber(count.toInt())
                    .setGroup(host)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)
                    .setMessageNotification(context)
                    .addReplyAction(context, pushMessage)

            val subText = RocketChatCache(context).getHostSiteName(pushMessage.host)
            if (subText.isNotEmpty()) {
                notificationBuilder.setSubText(subText)
            }

            channel.enableLights(true)
            channel.enableVibration(true)

            if ("inbox" == style) {
                val messages = messageStack.get(notificationId.toInt())
                val messageCount = messages.size
                if (messageCount > 1) {
                    val summary = summaryText.replace("%n%", messageCount.toString())
                            .fromHtml()
                    val inbox = Notification.InboxStyle()
                            .setBigContentTitle(title.fromHtml())
                            .setSummaryText(summary)

                    messages.forEach { msg ->
                        inbox.addLine(msg.fromHtml())
                    }

                    notificationBuilder.setStyle(inbox)
                } else {
                    val bigText = Notification.BigTextStyle()
                            .bigText(message.fromHtml())
                            .setBigContentTitle(title.fromHtml())

                    notificationBuilder.setStyle(bigText)
                }
            } else {
                notificationBuilder.setContentText(message.fromHtml())
            }

            return notificationBuilder.build()
        }
    }

    private fun bundleMessage(id: Int, message: CharSequence) {
        val existingStack: ArrayList<CharSequence>? = messageStack[id]

        if (existingStack == null) {
            val newStack = arrayListOf<CharSequence>()
            newStack.add(message)
            messageStack.put(id, newStack)
        } else {
            existingStack.add(0, message)
        }
    }

    private fun getDismissIntent(context: Context, notificationId: Int): PendingIntent {
        val deleteIntent = Intent(context, DeleteReceiver::class.java)
        deleteIntent.putExtra("notId", notificationId)
        return PendingIntent.getBroadcast(context, notificationId, deleteIntent, 0)
    }

    private fun getContentIntent(context: Context, notificationId: Int, pushMessage: PushMessage, group: Boolean = false): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.putExtra(PushConstants.NOT_ID, notificationId)
        notificationIntent.putExtra(PushConstants.HOSTNAME, pushMessage.host)
        if (!group) {
            notificationIntent.putExtra(PushConstants.ROOM_ID, pushMessage.rid)
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
        replyIntent.putExtra("push", pushMessage as Serializable)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Notification.Builder.setMessageNotification(ctx: Context): Notification.Builder {
        val res = ctx.resources
        val smallIcon = res.getIdentifier(
                "rocket_chat_notification", "drawable", ctx.packageName)
        with(this, {
            setAutoCancel(true)
            setShowWhen(true)
            setColor(res.getColor(R.color.colorRed400, ctx.theme))
            setDefaults(Notification.DEFAULT_ALL)
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
        replyIntent.putExtra("push", pushMessage as Serializable)
        val pendingIntent = PendingIntent.getBroadcast(
                context, randomizer.nextInt(), replyIntent, 0)
        val replyAction =
                NotificationCompat.Action.Builder(
                        R.drawable.ic_reply, REPLY_LABEL, pendingIntent)
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
            setColor(ctx.resources.getColor(R.color.colorRed400))
            setDefaults(Notification.DEFAULT_ALL)
            setSmallIcon(smallIcon)
        })
        return this
    }

    private data class PushMessage(val title: String,
                                   val message: String,
                                   val image: String?,
                                   val ejson: String,
                                   val count: String,
                                   val notificationId: String,
                                   val summaryText: String,
                                   val style: String) : Serializable {
        val host: String
        val rid: String
        val type: String
        val name: String?
        val sender: Sender
        val createdAt: Long

        init {
            val json = JSONObject(ejson)
            host = json.getString("host")
            rid = json.getString("rid")
            type = json.getString("type")
            name = json.optString("name")
            sender = Sender(json.getString("sender"))
            createdAt = System.currentTimeMillis()
        }

        data class Sender(val sender: String) : Serializable {
            val _id: String
            val username: String
            val name: String

            init {
                val json = JSONObject(sender)
                _id = json.getString("_id")
                username = json.getString("username")
                name = json.getString("name")
            }
        }
    }

    /**
     * BroadcastReceiver for dismissed notifications.
     */
    class DeleteReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notificationId = intent?.extras?.getInt("notId")
            if (notificationId != null) {
                PushManager.clearMessageBundle(notificationId)
            }
        }
    }

    /**
     * *EXPERIMENTAL*
     *
     * BroadcastReceiver for notifications' replies.
     */
    class ReplyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null) {
                return
            }

            synchronized(this) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val message: CharSequence? = extractMessage(intent)
                val pushMessage = intent?.extras?.getSerializable("push") as PushMessage?

                pushMessage?.let {
                    val userNotId = pushMessage.notificationId.toInt()
                    val groupTuple = groupMap.get(pushMessage.host)
                    messageStack[userNotId]?.let {
                        for (msg in messageStack[userNotId]) {
                            manager.cancel(userNotId)
                            groupTuple?.second?.decrementAndGet()
                        }
                        clearMessageBundle(userNotId)
                        groupTuple?.let {
                            val groupNotId = groupTuple.first
                            val totalNot = groupTuple.second.get()
                            if (totalNot == 0) {
                                manager.cancel(groupNotId)
                            }
                        }
                        message?.let {
                            sendMessage(context, message, pushMessage.rid)
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
        private fun sendMessage(ctx: Context, message: CharSequence, roomId: String) {
            val hostname = RocketChatCache(ctx).selectedServerHostname
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
                    .subscribe(
                            { success ->
                                // Empty
                            },
                            { throwable ->
                                Logger.report(throwable)
                            })
        }
    }
}