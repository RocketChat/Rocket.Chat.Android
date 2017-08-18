import chat.rocket.android.widget.helper.UserAvatarHelper
import org.junit.Test
import kotlin.test.assertEquals

class UserAvatarHelperTest {

    @Test
    fun getUsernameInitialsTest() {
        assertEquals("?", UserAvatarHelper.getUsernameInitials(""))
        assertEquals("?", UserAvatarHelper.getUsernameInitials("?"))
        assertEquals("F", UserAvatarHelper.getUsernameInitials("f"))
        assertEquals("B", UserAvatarHelper.getUsernameInitials("B"))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("Fo"))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("FO"))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("fOo"))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("FOO"))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("F.O"))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("F.o"))
        assertEquals("FB", UserAvatarHelper.getUsernameInitials("Foo.bar"))
        assertEquals("FB", UserAvatarHelper.getUsernameInitials("Foobar.bar"))
        assertEquals("FZ", UserAvatarHelper.getUsernameInitials("Foobar.bar.zab"))
        assertEquals("..", UserAvatarHelper.getUsernameInitials(".."))
        assertEquals("..", UserAvatarHelper.getUsernameInitials("..."))
        assertEquals(".F", UserAvatarHelper.getUsernameInitials(".Foo."))
        assertEquals("FO", UserAvatarHelper.getUsernameInitials("Foo.."))
    }
}