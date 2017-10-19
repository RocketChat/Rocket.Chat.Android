package chat.rocket.android.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.util.SparseArray
import chat.rocket.android.BuildConfig
import chat.rocket.android.RocketChatCache
import chat.rocket.android.activity.MainActivity
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

object PushManager {

    // Map associating a notification id to a list of corresponding messages ie. an id corresponds
    // to a user and the corresponding list is all the messages sent by him.
    val messageStack = SparseArray<ArrayList<String>>()
    // Notifications received from the same server are grouped in a single bundled notification.
    // This map associates a host to a group id.
    val groupMap = HashMap<String, Int>()
    val randomizer = Random()

    fun handle(context: Context, data: Bundle) {
        val appContext = context.applicationContext
        val message = data["message"] as String
        val image = data["image"] as String
        val ejson = data["ejson"] as String
        val notificationId = data["notId"] as String
        val style = data["style"] as String
        val summaryText = data["summaryText"] as String
        val count = data["count"] as String
        val title = data["title"] as String
        val pushMessage = PushMessage(title, message, image, ejson, count, notificationId, summaryText, style)

        // We should use Timber here
        if (BuildConfig.DEBUG) {
            Log.d(PushMessage::class.java.simpleName, pushMessage.toString())
        }

        val res = appContext.resources

        val smallIcon = res.getIdentifier("rocket_chat_notification", "drawable", appContext.packageName)

        stackMessage(notificationId.toInt(), pushMessage.message)

        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val groupNotification = createGroupNotification(context, pushMessage, data, smallIcon)

        val groupId = groupMap.get(pushMessage.host)
        val notification: Notification
        if (isAndroidVersionAtLeast(Build.VERSION_CODES.O)) {
            notification = createNotificationForOreoAndAbove(appContext, pushMessage, smallIcon, data)
            if (groupId != null) {
                notificationManager.notify(groupId, notification)
            }
            notificationManager.notify(notificationId.toInt(), notification)
        } else {
            notification = createCompatNotification(appContext, pushMessage, smallIcon, data)
            if (groupId != null) {
                NotificationManagerCompat.from(appContext).notify(groupId, groupNotification)
            }
            NotificationManagerCompat.from(appContext).notify(notificationId.toInt(), notification)
        }
    }

    private fun isAndroidVersionAtLeast(minVersion: Int) = Build.VERSION.SDK_INT >= minVersion

    private fun addGroupToBundle(host: String) {
        val size = groupMap.size
        if (groupMap.get(host) == null) {
            groupMap.put(host, size + 1)
        }
    }

    fun clearMessageStack(notificationId: Int) {
        messageStack.delete(notificationId)
    }

    private fun createGroupNotification(context: Context, pushMessage: PushMessage, data: Bundle, smallIcon: Int): Notification {
        // Create notification group.
        addGroupToBundle(pushMessage.host)
        val id = pushMessage.notificationId.toInt()
        val contentIntent = getContentIntent(context, id, data, pushMessage)
        val deleteIntent = getDismissIntent(context, id)
        val notGroupBuilder = NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(pushMessage.createdAt)
                .setContentTitle(pushMessage.title.fromHtml())
                .setContentText(pushMessage.message.fromHtml())
                .setGroup(pushMessage.host)
                .setGroupSummary(true)
                .setSmallIcon(smallIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushMessage.message.fromHtml()))
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)

        val subText = RocketChatCache(context).getHostSiteName(pushMessage.host)
        if (subText.isNotEmpty()) {
            notGroupBuilder.setSubText(subText)
        }

        return notGroupBuilder.build()
    }

    private fun createCompatNotification(context: Context, pushMessage: PushMessage, smallIcon: Int, data: Bundle): Notification {
        with(pushMessage) {
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, data, pushMessage)
            val deleteIntent = getDismissIntent(context, id)

            val notificationBuilder = NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setNumber(count.toInt())
                    .setGroup(host)
                    .setSmallIcon(smallIcon)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)

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
    private fun createNotificationForOreoAndAbove(context: Context, pushMessage: PushMessage, smallIcon: Int, data: Bundle): Notification {
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        with(pushMessage) {
            val id = notificationId.toInt()
            val contentIntent = getContentIntent(context, id, data, pushMessage)
            val deleteIntent = getDismissIntent(context, id)

            val channel = NotificationChannel(notificationId, sender.username, NotificationManager.IMPORTANCE_HIGH)
            val notificationBuilder = Notification.Builder(context, pushMessage.rid)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setWhen(createdAt)
                    .setContentTitle(title.fromHtml())
                    .setContentText(message.fromHtml())
                    .setNumber(count.toInt())
                    .setGroup(host)
                    .setSmallIcon(smallIcon)
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(contentIntent)

            val subText = RocketChatCache(context).getHostSiteName(pushMessage.host)
            if (subText.isNotEmpty()) {
                notificationBuilder.setSubText(subText)
            }

            channel.enableLights(true)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
            return notificationBuilder.build()
        }
    }

    private fun stackMessage(id: Int, message: String) {
        val existingStack: ArrayList<String>? = messageStack[id]

        if (existingStack == null) {
            val newStack = arrayListOf<String>()
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

    private fun getContentIntent(context: Context, notificationId: Int, extras: Bundle, pushMessage: PushMessage): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.putExtra(PushConstants.PUSH_BUNDLE, extras)
        notificationIntent.putExtra(PushConstants.NOT_ID, notificationId)
        notificationIntent.putExtra(PushConstants.HOSTNAME, pushMessage.host)
        notificationIntent.putExtra(PushConstants.ROOM_ID, pushMessage.rid)

        return PendingIntent.getActivity(context, randomizer.nextInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private data class PushMessage(val title: String,
                           val message: String,
                           val image: String?,
                           val ejson: String,
                           val count: String,
                           val notificationId: String,
                           val summaryText: String,
                           val style: String) {
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

        data class Sender(val sender: String) {
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

    class DeleteReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notificationId = intent?.extras?.getInt("notId")
            if (notificationId != null) {
                PushManager.clearMessageStack(notificationId)
            }
        }
    }

    // String extensions
    fun String.fromHtml(): Spanned {
        return Html.fromHtml(this)
    }
}