import chat.rocket.android.helper.RocketChatUserAvatar
import org.junit.Test

class RocketChatUserAvatarTest {

    @Test
    fun `Test username initials`() {
        assert(RocketChatUserAvatar.getUsernameInitials("") == "?")
        assert(RocketChatUserAvatar.getUsernameInitials("?") == "?")
        assert(RocketChatUserAvatar.getUsernameInitials("f") == "F")
        assert(RocketChatUserAvatar.getUsernameInitials("B") == "B")
        assert(RocketChatUserAvatar.getUsernameInitials("fo") == "FO")
        assert(RocketChatUserAvatar.getUsernameInitials("FO") == "FO")
        assert(RocketChatUserAvatar.getUsernameInitials("fOo") == "FO")
        assert(RocketChatUserAvatar.getUsernameInitials("FOO") == "FO")
        assert(RocketChatUserAvatar.getUsernameInitials("F.O") == "FO")
        assert(RocketChatUserAvatar.getUsernameInitials("F.o") == "FO")
        assert(RocketChatUserAvatar.getUsernameInitials("Foo.bar") == "FB")
        assert(RocketChatUserAvatar.getUsernameInitials("Foobar.bar") == "FB")
        assert(RocketChatUserAvatar.getUsernameInitials("Foobar.bar.zab") == "FZ")
    }
}