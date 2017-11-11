package chat.rocket.android.helper

import org.junit.Test
import kotlin.test.assertEquals

class UrlHelperTest {

    @Test
    fun removeUriSchemeTest() {
        assertEquals("open.rocket.chat", UrlHelper.removeUriScheme("https://open.rocket.chat"))
        assertEquals("open.rocket.chat", UrlHelper.removeUriScheme("http://open.rocket.chat"))
        assertEquals("open.rocket.chat", UrlHelper.removeUriScheme("open.rocket.chat"))
    }

    @Test
    fun getSafeHostnameTest() {
        assertEquals("https://open.rocket.chat", UrlHelper.getSafeHostname("https://open.rocket.chat"))
        assertEquals("https://open.rocket.chat", UrlHelper.getSafeHostname("http://open.rocket.chat"))
        assertEquals("https://open.rocket.chat", UrlHelper.getSafeHostname("open.rocket.chat"))
     }

    @Test
    fun getUrlTest() {
        assertEquals("https://open.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("https://open.rocket.chat/GENERAL/file.txt"))
        assertEquals("http://open.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("http://open.rocket.chat/GENERAL/file.txt"))
        assertEquals("open.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("open.rocket.chat/GENERAL/file.txt"))
        assertEquals("open.rocket.chat/GENERAL/a%20sample%20file.txt", UrlHelper.getSafeUrl("open.rocket.chat/GENERAL/a sample file.txt"))
        assertEquals("open.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("open.rocket.chat\\/GENERAL\\/file.txt"))
    }

    @Test
    fun getAttachmentLinkTest() {
        assertEquals("https://open.rocket.chat/file-upload/aFileId/aFileName.txt", UrlHelper.getAttachmentLink("https://open.rocket.chat", "aFileId", "aFileName.txt"))
        assertEquals("https://open.rocket.chat/file-upload/aFileId/aFileName.txt", UrlHelper.getAttachmentLink("http://open.rocket.chat", "aFileId", "aFileName.txt"))
        assertEquals("https://open.rocket.chat/file-upload/aFileId/aFileName.txt", UrlHelper.getAttachmentLink("open.rocket.chat", "aFileId", "aFileName.txt"))
    }
}