package chat.rocket.android.push

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.push.PushManager.PushMessage
import com.nhaarman.mockito_kotlin.*
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class,
        application = PushManagerTest.StubApplication::class,
        sdk = intArrayOf(23))
class PushManagerTest {

    val EJSON = """
        {
           "host":"https://open.rocket.chat/",
           "rid":"FaXMyHqbNJbPq6Ym9uWiyfQkgekhXywvKw",
           "sender":{
              "_id":"uWiFa3adOi0adac",
              "username":"jean-luc.picard",
              "name":"Jean-Luc Picard"
           },
           "type":"d",
           "name":null
        }
        """

    val EJSON_NO_SENDER = """
        {
           "host":"https://open.rocket.chat/",
           "rid":"FaXMyHqbNJbPq6Ym9uWiyfQkgekhXywvKw",
           "sender":null,
           "type":"d",
           "name":null
        }
        """

    lateinit var data: Bundle
    lateinit var pushManager: PushManager
    lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        data = Bundle()
        data.putString("message", "Hello")
        data.putString("title", "jean-luc.picard")
        data.putString("ejson", EJSON)
        data.putString("notId", "1")
        context = spy(RuntimeEnvironment.application)
        pushManager = spy(PushManager)
        val res = mock<Resources> {
            on { getColor(any()) } doReturn 0

            on { getIdentifier(
                    anyString(), anyString(), any()) } doReturn R.drawable.notification_icon_background

            on { getConfiguration() } doReturn RuntimeEnvironment.application.resources.configuration
        }
        whenever(context.resources).doReturn(res)
        whenever(context.applicationContext).doReturn(context)
    }

    @Test
    fun `should create PushMessage without throwing`() {
        PushMessage(null, null, null, null, null, "xxx",
                null, null)
    }

    @Test
    fun `given data shoud show notification`() {
        pushManager.handle(context, data)
        val push = PushMessage(title = data["title"] as String,
                message = data["message"] as String, ejson = EJSON, notificationId = "1")
        verify(pushManager, times(1)).showNotification(context, push)
    }

    @Test
    fun `given required data is missing do not show notification`() {
        val bundle = Bundle()
        pushManager.handle(context, bundle)
        verify(pushManager, never()).showNotification(any(), any())

        bundle.putString("title", "jean-luc.picard")
        bundle.putString("message", "Hello!")
        pushManager.handle(context, bundle)
        verify(pushManager, never()).showNotification(any(), any())

        bundle.clear()
        bundle.putString("ejson", EJSON)
        bundle.putString("message", "Hello!")
        pushManager.handle(context, bundle)
        verify(pushManager, never()).showNotification(any(), any())

        bundle.clear()
        bundle.putString("ejson", EJSON)
        bundle.putString("title", "jean-luc.picard")
        pushManager.handle(context, bundle)
        verify(pushManager, never()).showNotification(any(), any())
    }

    @Test
    fun `given data should deserialize correctly`() {
        pushManager.handle(context, data)
        val push = PushMessage(
                data.getString("title"),
                data.getString("message"),
                null,
                EJSON,
                null,
                data.getString("notId"),
                null,
                null
        )
        verify(pushManager, times(1)).showNotification(context, push)

        push.title `should equal` "jean-luc.picard"
        push.message `should equal` "Hello"
        val sender = push.sender
        assertTrue(sender != null)
        sender?._id `should equal` "uWiFa3adOi0adac"
        sender?.name `should equal` "Jean-Luc Picard"
        sender?.username `should equal` "jean-luc.picard"
    }

    @Test
    fun `given that only sender is missing show notification`() {
        val bundle = Bundle()
        bundle.putString("title", "jean-luc.picard")
        bundle.putString("message", "Hello")
        bundle.putString("ejson", EJSON_NO_SENDER)

        pushManager.handle(context, bundle)
        verify(pushManager, times(1)).showNotification(any(), any())
    }

    internal class StubApplication : Application()
}