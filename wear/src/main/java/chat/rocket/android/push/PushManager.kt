package chat.rocket.android.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import android.text.Spanned
import androidx.core.app.NotificationCompat
import chat.rocket.android.R
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.runBlocking
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class PushManager @Inject constructor(
    private val manager: NotificationManager,
    private val moshi: Moshi,
    private val context: Context
) {
    private val random = Random()

    @Synchronized
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
            val adapter = moshi.adapter<PushInfo>(PushInfo::class.java)

            val pushMessage = if (ejson != null) {
                val info = adapter.fromJson(ejson)
                PushMessage(title!!, message!!, info!!, image, count, notId, summaryText, style)
            } else {
                PushMessage(
                    title!!,
                    message!!,
                    PushInfo.EMPTY,
                    image,
                    count,
                    notId,
                    summaryText,
                    style
                )
            }

            Timber.d("Received push message: $pushMessage")

            showNotification(pushMessage)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing PUSH message: $data")
            ex.printStackTrace()
        }
    }

    suspend fun showNotification(pushMessage: PushMessage) {
        val notId = pushMessage.notificationId.toInt()
        if (pushMessage.info.host.isNotEmpty()) {
            val notification = createSingleNotification(pushMessage)
            notification?.let {
                manager.notify(notId, it)
            }
        }
    }

    private fun createSingleNotification(pushMessage: PushMessage): Notification? {
        with(pushMessage) {
            val builder = createNotificationBuilder(pushMessage)
            val bigText = NotificationCompat.BigTextStyle()
                .bigText(message.fromHtml())
                .setBigContentTitle(title.fromHtml())

            builder.setStyle(bigText)

            return builder.build()
        }
    }

    private fun createNotificationBuilder(pushMessage: PushMessage): NotificationCompat.Builder {
        return with(pushMessage) {
            val host = info.host

            val builder = NotificationCompat.Builder(context, host)
                .setWhen(info.createdAt)
                .setContentTitle(title.fromHtml())
                .setContentText(message.fromHtml())
                .setSmallIcon(R.drawable.notification_small_icon)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = host
                val channelName = host
                val channel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                channel.enableLights(false)
                channel.enableVibration(true)
                channel.setShowBadge(true)
                manager.createNotificationChannel(channel)
                builder.setChannelId(channelId)
            }
            return@with builder
        }
    }

    private fun CharSequence.fromHtml(): Spanned {
        return Html.fromHtml(this as String)
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
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(PushMessage::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeString(message)
            parcel.writeParcelable(info, flags)
            parcel.writeString(image)
            parcel.writeString(count)
            parcel.writeString(notificationId)
            parcel.writeString(summaryText)
            parcel.writeString(style)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PushMessage> {
            override fun createFromParcel(parcel: Parcel): PushMessage {
                return PushMessage(parcel)
            }

            override fun newArray(size: Int): Array<PushMessage?> {
                return arrayOfNulls(size)
            }
        }
    }

    @JsonSerializable
    data class PushInfo @KotshiConstructor constructor(
        @Json(name = "host") val hostname: String,
        @Json(name = "rid") val roomId: String,
        val type: RoomType,
        val name: String?,
        val sender: PushSender?
    ) : Parcelable {
        val createdAt: Long
            get() = System.currentTimeMillis()
        val host by lazy {
            sanitizeUrl(hostname)
        }

        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            roomTypeOf(parcel.readString()),
            parcel.readString(),
            parcel.readParcelable(PushInfo::class.java.classLoader)
        )

        private fun sanitizeUrl(baseUrl: String): String {
            var url = baseUrl.trim()
            while (url.endsWith('/')) {
                url = url.dropLast(1)
            }

            return url
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(hostname)
            parcel.writeString(roomId)
            parcel.writeString(type.toString())
            parcel.writeString(name)
            parcel.writeParcelable(sender, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PushInfo> {
            val EMPTY = PushInfo(
                hostname = "", roomId = "", type = roomTypeOf(RoomType.CHANNEL), name = "",
                sender = null
            )

            override fun createFromParcel(parcel: Parcel): PushInfo {
                return PushInfo(parcel)
            }

            override fun newArray(size: Int): Array<PushInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    @JsonSerializable
    data class PushSender @KotshiConstructor constructor(
        @Json(name = "_id") val id: String,
        val username: String?,
        val name: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(username)
            parcel.writeString(name)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PushSender> {
            override fun createFromParcel(parcel: Parcel): PushSender {
                return PushSender(parcel)
            }

            override fun newArray(size: Int): Array<PushSender?> {
                return arrayOfNulls(size)
            }
        }
    }
}