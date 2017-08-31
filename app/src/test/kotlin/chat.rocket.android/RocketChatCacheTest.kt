package chat.rocket.android;

import android.content.Context
import org.hamcrest.CoreMatchers.equalTo
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RocketChatCacheTest {

    lateinit var cache: RocketChatCache

    @Before
    fun setup() {
        val mockedContext = mock(Context::class.java)
        val mockAppContext = mock(Context::class.java)
        `when`(mockedContext.applicationContext).thenReturn(mockAppContext)
        cache = spy(RocketChatCache(mockedContext))
    }

    @Test
    fun getServerList_ShouldReturnHostnameList() {
        val hostnameList = JSONObject()
                .put("demo.rocket.chat", "imageuri")
                .put("192.168.0.6:3000", "imageuri")
                .toString()

        doReturn(hostnameList).`when`(cache).getString("KEY_HOSTNAME_LIST", null)

        val expectedServerList = mutableListOf("demo.rocket.chat", "192.168.0.6:3000")
        val serverList = cache.serverList
        expectedServerList.sort()
        serverList.sort()
        assertThat(cache.serverList, equalTo(expectedServerList))
    }
}