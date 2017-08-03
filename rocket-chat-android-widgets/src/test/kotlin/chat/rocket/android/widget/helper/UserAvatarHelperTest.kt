import chat.rocket.android.widget.helper.UserAvatarHelper
import org.junit.Test

class UserAvatarHelperTest {

    @Test
    fun getUsernameInitialsTest() {
        assert(UserAvatarHelper.getUsernameInitials("") == "?")
        assert(UserAvatarHelper.getUsernameInitials("?") == "?")
        assert(UserAvatarHelper.getUsernameInitials("f") == "F")
        assert(UserAvatarHelper.getUsernameInitials("B") == "B")
        assert(UserAvatarHelper.getUsernameInitials("fo") == "FO")
        assert(UserAvatarHelper.getUsernameInitials("FO") == "FO")
        assert(UserAvatarHelper.getUsernameInitials("fOo") == "FO")
        assert(UserAvatarHelper.getUsernameInitials("FOO") == "FO")
        assert(UserAvatarHelper.getUsernameInitials("F.O") == "FO")
        assert(UserAvatarHelper.getUsernameInitials("F.o") == "FO")
        assert(UserAvatarHelper.getUsernameInitials("Foo.bar") == "FB")
        assert(UserAvatarHelper.getUsernameInitials("Foobar.bar") == "FB")
        assert(UserAvatarHelper.getUsernameInitials("Foobar.bar.zab") == "FZ")
    }
}