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
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.text.Html
import android.text.Spanned
import android.util.SparseArray
import org.json.JSONObject

object PushManager {

    val messageStack = SparseArray<ArrayList<String>>()

    fun handle(context: Context, data: Bundle) {
        val now = System.currentTimeMillis()
        val appContext = context.applicationContext
        val notificationId = data["notId"] as String
        val style = data["style"] as String
        val summaryText = data["summaryText"] as String
        val count = data["count"] as String
        val pushMessage = PushMessage(data["title"] as String,
                data["message"] as String,
                data["image"] as String,
                data["ejson"] as String)

        val res = appContext.resources

        val smallIcon = res.getIdentifier("rocket_chat_notification", "drawable", appContext.packageName)

        stackMessage(notificationId.toInt(), pushMessage.message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            with(pushMessage) {
                val channel = NotificationChannel(notificationId, sender.username, NotificationManager.IMPORTANCE_HIGH)
                val notification = Notification.Builder(appContext, pushMessage.rid)
                        .setAutoCancel(true)
                        .setShowWhen(true)
                        .setWhen(now)
                        .setContentTitle(title.fromHtml())
                        .setContentText(message.fromHtml())
                        .setNumber(count.toInt())
                        .setSmallIcon(smallIcon)
                        .build()

                channel.enableLights(true)
                channel.enableVibration(true)

                notificationManager.createNotificationChannel(channel)
                notificationManager.notify(notificationId.toInt(), notification)
            }
        } else {
            with(pushMessage) {
                val notificationBuilder = NotificationCompat.Builder(appContext)
                        .setAutoCancel(true)
                        .setShowWhen(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(now)
                        .setContentTitle(title.fromHtml())
                        .setContentText(message.fromHtml())
                        .setNumber(count.toInt())
                        .setSmallIcon(smallIcon)
                        .setDeleteIntent(getDismissIntent(appContext, notificationId.toInt()))

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

                val notification = notificationBuilder.build()
                NotificationManagerCompat.from(appContext).notify(notificationId.toInt(), notification)
            }
        }
    }

    fun clearStack(notificationId: Int) {
        messageStack.delete(notificationId)
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

    data class PushMessage(val title: String, val message: String, val image: String?, val ejson: String) {
        val host: String
        val rid: String
        val type: String
        val name: String?
        val sender: Sender

        init {
            val json = JSONObject(ejson)
            host = json.getString("host")
            rid = json.getString("rid")
            type = json.getString("type")
            name = json.optString("name")
            sender = Sender(json.getString("sender"))
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
                PushManager.clearStack(notificationId)
            }
        }
    }

    // String extensions
    fun String.fromHtml(): Spanned {
        return Html.fromHtml(this)
    }
}