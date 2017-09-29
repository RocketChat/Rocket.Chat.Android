package chat.rocket.android.api.rest

import chat.rocket.core.models.Room
import org.junit.Test
import kotlin.test.assertEquals

class RestApiHelperTest {

    @Test
    fun getEndpointUrlForMessagesTest() {
        assertEquals("https://demo.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_PRIVATE, "demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "demo.rocket.chat"))

        assertEquals("https://demo.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_PRIVATE, "https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "https://demo.rocket.chat"))

        assertEquals("https://demo.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_PRIVATE, "http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "http://demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_PRIVATE, "www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "www.demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "https://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_PRIVATE, "https://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "https://www.demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "http://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_PRIVATE, "http://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "http://www.demo.rocket.chat"))
    }

    @Test
    fun getEndpointUrlForFileListTest() {
        assertEquals("https://demo.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_PRIVATE, "demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "demo.rocket.chat"))

        assertEquals("https://demo.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_PRIVATE, "https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "https://demo.rocket.chat"))

        assertEquals("https://demo.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_PRIVATE, "http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "http://demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_PRIVATE, "www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "www.demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "https://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_PRIVATE, "https://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "https://www.demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "http://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_PRIVATE, "http://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "http://www.demo.rocket.chat"))
    }

    @Test
    fun getEndpointUrlForMemberListTest() {
        assertEquals("https://demo.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_PRIVATE, "demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "demo.rocket.chat"))

        assertEquals("https://demo.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_PRIVATE, "https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "https://demo.rocket.chat"))

        assertEquals("https://demo.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_PRIVATE, "http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "http://demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_PRIVATE, "www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "www.demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "https://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_PRIVATE, "https://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "https://www.demo.rocket.chat"))

        assertEquals("https://www.demo.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "http://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_PRIVATE, "http://www.demo.rocket.chat"))
        assertEquals("https://www.demo.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "http://www.demo.rocket.chat"))
    }

    @Test
    fun getRestApiUrlForMessagesTest() {
        assertEquals("/api/v1/channels.messages", RestApiHelper.getRestApiUrlForMessages(Room.TYPE_CHANNEL))
        assertEquals("/api/v1/groups.messages", RestApiHelper.getRestApiUrlForMessages(Room.TYPE_PRIVATE))
        assertEquals("/api/v1/dm.messages", RestApiHelper.getRestApiUrlForMessages(Room.TYPE_DIRECT_MESSAGE))
    }

    @Test
    fun getRestApiUrlForFileListTest() {
        assertEquals("/api/v1/channels.files", RestApiHelper.getRestApiUrlForFileList(Room.TYPE_CHANNEL))
        assertEquals("/api/v1/groups.files", RestApiHelper.getRestApiUrlForFileList(Room.TYPE_PRIVATE))
        assertEquals("/api/v1/dm.files", RestApiHelper.getRestApiUrlForFileList(Room.TYPE_DIRECT_MESSAGE))
    }

    @Test
    fun getRestApiUrlForMemberListTest() {
        assertEquals("/api/v1/channels.members", RestApiHelper.getRestApiUrlForMemberList(Room.TYPE_CHANNEL))
        assertEquals("/api/v1/groups.members", RestApiHelper.getRestApiUrlForMemberList(Room.TYPE_PRIVATE))
        assertEquals("/api/v1/dm.members", RestApiHelper.getRestApiUrlForMemberList(Room.TYPE_DIRECT_MESSAGE))
    }
}