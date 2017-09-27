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
        assertEquals("https://demo.rocket.chat/GENERAL/file.txt", UrlHelper.getUrl("https://demo.rocket.chat/GENERAL/file.txt"))
        assertEquals("http://demo.rocket.chat/GENERAL/file.txt", UrlHelper.getUrl("http://demo.rocket.chat/GENERAL/file.txt"))
        assertEquals("demo.rocket.chat/GENERAL/file.txt", UrlHelper.getUrl("demo.rocket.chat/GENERAL/file.txt"))
        assertEquals("demo.rocket.chat/GENERAL/a%20sample%20file.txt", UrlHelper.getUrl("demo.rocket.chat/GENERAL/a sample file.txt"))
        assertEquals("demo.rocket.chat/GENERAL/file.txt", UrlHelper.getUrl("demo.rocket.chat\\/GENERAL\\/file.txt"))
    }

    @Test
    fun getUrlForFileTest() {
        assertEquals("https://demo.rocket.chat/GENERAL/file.txt?rc_uid=userId&rc_token=token", UrlHelper.getUrlForFile("https://demo.rocket.chat/GENERAL/file.txt","userId", "token"))
        assertEquals("https://demo.rocket.chat/GENERAL/file.txt?rc_uid=userId&rc_token=token", UrlHelper.getUrlForFile("http://demo.rocket.chat/GENERAL/file.txt","userId", "token"))
        assertEquals("https://demo.rocket.chat/GENERAL/file.txt?rc_uid=userId&rc_token=token", UrlHelper.getUrlForFile("demo.rocket.chat/GENERAL/file.txt","userId", "token"))

        assertEquals("https://demo.rocket.chat/GENERAL/a%20sample%20file.txt?rc_uid=userId&rc_token=token", UrlHelper.getUrlForFile("demo.rocket.chat/GENERAL/a sample file.txt","userId", "token"))
        assertEquals("https://demo.rocket.chat/GENERAL/file.txt?rc_uid=userId&rc_token=token", UrlHelper.getUrlForFile("demo.rocket.chat\\/GENERAL\\/file.txt","userId", "token"))
    }
}