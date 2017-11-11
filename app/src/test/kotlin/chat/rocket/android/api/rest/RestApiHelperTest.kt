package chat.rocket.android.api.rest

import chat.rocket.core.models.Room
import org.junit.Test
import kotlin.test.assertEquals

class RestApiHelperTest {

    @Test
    fun getEndpointUrlForMessagesTest() {
        assertEquals("https://open.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_GROUP, "open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "open.rocket.chat"))

        assertEquals("https://open.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_GROUP, "https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "https://open.rocket.chat"))

        assertEquals("https://open.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_GROUP, "http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "http://open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_GROUP, "www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "www.open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "https://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_GROUP, "https://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "https://www.open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_CHANNEL, "http://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_GROUP, "http://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.messages", RestApiHelper.getEndpointUrlForMessages(Room.TYPE_DIRECT_MESSAGE, "http://www.open.rocket.chat"))
    }

    @Test
    fun getEndpointUrlForFileListTest() {
        assertEquals("https://open.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_GROUP, "open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "open.rocket.chat"))

        assertEquals("https://open.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_GROUP, "https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "https://open.rocket.chat"))

        assertEquals("https://open.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_GROUP, "http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "http://open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_GROUP, "www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "www.open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "https://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_GROUP, "https://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "https://www.open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_CHANNEL, "http://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_GROUP, "http://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.files", RestApiHelper.getEndpointUrlForFileList(Room.TYPE_DIRECT_MESSAGE, "http://www.open.rocket.chat"))
    }

    @Test
    fun getEndpointUrlForMemberListTest() {
        assertEquals("https://open.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_GROUP, "open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "open.rocket.chat"))

        assertEquals("https://open.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_GROUP, "https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "https://open.rocket.chat"))

        assertEquals("https://open.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_GROUP, "http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "http://open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_GROUP, "www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "www.open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "https://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_GROUP, "https://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "https://www.open.rocket.chat"))

        assertEquals("https://www.open.rocket.chat/api/v1/channels.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_CHANNEL, "http://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/groups.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_GROUP, "http://www.open.rocket.chat"))
        assertEquals("https://www.open.rocket.chat/api/v1/dm.members", RestApiHelper.getEndpointUrlForMemberList(Room.TYPE_DIRECT_MESSAGE, "http://www.open.rocket.chat"))
    }

    @Test
    fun getRestApiUrlForMessagesTest() {
        assertEquals("/api/v1/channels.messages", RestApiHelper.getRestApiUrlForMessages(Room.TYPE_CHANNEL))
        assertEquals("/api/v1/groups.messages", RestApiHelper.getRestApiUrlForMessages(Room.TYPE_GROUP))
        assertEquals("/api/v1/dm.messages", RestApiHelper.getRestApiUrlForMessages(Room.TYPE_DIRECT_MESSAGE))
    }

    @Test
    fun getRestApiUrlForFileListTest() {
        assertEquals("/api/v1/channels.files", RestApiHelper.getRestApiUrlForFileList(Room.TYPE_CHANNEL))
        assertEquals("/api/v1/groups.files", RestApiHelper.getRestApiUrlForFileList(Room.TYPE_GROUP))
        assertEquals("/api/v1/dm.files", RestApiHelper.getRestApiUrlForFileList(Room.TYPE_DIRECT_MESSAGE))
    }

    @Test
    fun getRestApiUrlForMemberListTest() {
        assertEquals("/api/v1/channels.members", RestApiHelper.getRestApiUrlForMemberList(Room.TYPE_CHANNEL))
        assertEquals("/api/v1/groups.members", RestApiHelper.getRestApiUrlForMemberList(Room.TYPE_GROUP))
        assertEquals("/api/v1/dm.members", RestApiHelper.getRestApiUrlForMemberList(Room.TYPE_DIRECT_MESSAGE))
    }
}