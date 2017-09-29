import chat.rocket.android.widget.helper.AvatarHelper
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 8/2/17.
 */
class AvatarHelperTest {

    @Test
    fun getUsernameInitialsTest() {
        assertEquals("?", AvatarHelper.getUsernameInitials(""))
        assertEquals("?", AvatarHelper.getUsernameInitials("?"))
        assertEquals("F", AvatarHelper.getUsernameInitials("f"))
        assertEquals("B", AvatarHelper.getUsernameInitials("B"))
        assertEquals("FO", AvatarHelper.getUsernameInitials("Fo"))
        assertEquals("FO", AvatarHelper.getUsernameInitials("FO"))
        assertEquals("FO", AvatarHelper.getUsernameInitials("fOo"))
        assertEquals("FO", AvatarHelper.getUsernameInitials("FOO"))
        assertEquals("FO", AvatarHelper.getUsernameInitials("F.O"))
        assertEquals("FO", AvatarHelper.getUsernameInitials("F.o"))
        assertEquals("FB", AvatarHelper.getUsernameInitials("Foo.bar"))
        assertEquals("FB", AvatarHelper.getUsernameInitials("Foobar.bar"))
        assertEquals("FZ", AvatarHelper.getUsernameInitials("Foobar.bar.zab"))
        assertEquals("..", AvatarHelper.getUsernameInitials(".."))
        assertEquals("..", AvatarHelper.getUsernameInitials("..."))
        assertEquals(".F", AvatarHelper.getUsernameInitials(".Foo."))
        assertEquals("FO", AvatarHelper.getUsernameInitials("Foo.."))
    }
}