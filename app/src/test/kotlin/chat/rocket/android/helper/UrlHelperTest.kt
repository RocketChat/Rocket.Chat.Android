package chat.rocket.android.helper

import org.junit.Test
import kotlin.test.assertEquals

class UrlHelperTest {

    @Test
    fun removeUriSchemeTest() {
        assertEquals("demo.rocket.chat", UrlHelper.removeUriScheme("https://demo.rocket.chat"))
        assertEquals("demo.rocket.chat", UrlHelper.removeUriScheme("http://demo.rocket.chat"))
        assertEquals("demo.rocket.chat", UrlHelper.removeUriScheme("demo.rocket.chat"))
    }

    @Test
    fun getSafeHostnameTest() {
        assertEquals("https://demo.rocket.chat", UrlHelper.getSafeHostname("https://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat", UrlHelper.getSafeHostname("http://demo.rocket.chat"))
        assertEquals("https://demo.rocket.chat", UrlHelper.getSafeHostname("demo.rocket.chat"))
     }

    @Test
    fun getUrlTest() {
        assertEquals("https://demo.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("https://demo.rocket.chat/GENERAL/file.txt"))
        assertEquals("http://demo.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("http://demo.rocket.chat/GENERAL/file.txt"))
        assertEquals("demo.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("demo.rocket.chat/GENERAL/file.txt"))
        assertEquals("demo.rocket.chat/GENERAL/a%20sample%20file.txt", UrlHelper.getSafeUrl("demo.rocket.chat/GENERAL/a sample file.txt"))
        assertEquals("demo.rocket.chat/GENERAL/file.txt", UrlHelper.getSafeUrl("demo.rocket.chat\\/GENERAL\\/file.txt"))
    }

    @Test
    fun getAttachmentLinkTest() {
        assertEquals("https://demo.rocket.chat/file-upload/aFileId/aFileName.txt", UrlHelper.getAttachmentLink("https://demo.rocket.chat", "aFileId", "aFileName.txt"))
        assertEquals("https://demo.rocket.chat/file-upload/aFileId/aFileName.txt", UrlHelper.getAttachmentLink("http://demo.rocket.chat", "aFileId", "aFileName.txt"))
        assertEquals("https://demo.rocket.chat/file-upload/aFileId/aFileName.txt", UrlHelper.getAttachmentLink("demo.rocket.chat", "aFileId", "aFileName.txt"))
    }
}