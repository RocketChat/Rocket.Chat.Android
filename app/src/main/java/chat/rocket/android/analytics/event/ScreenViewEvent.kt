package chat.rocket.android.analytics.event

sealed class ScreenViewEvent(val screenName: String) {

    object About : ScreenViewEvent("AboutFragment")
    object ChatRoom : ScreenViewEvent("ChatRoomFragment")
    object ChatRooms : ScreenViewEvent("ChatRoomsFragment")
    object CreateChannel : ScreenViewEvent("CreateChannelFragment")
    object FavoriteMessages : ScreenViewEvent("FavoriteMessagesFragment")
    object Files : ScreenViewEvent("FilesFragment")
    object Login : ScreenViewEvent("LoginFragment")
    object MemberBottomSheet : ScreenViewEvent("MemberBottomSheetFragment")
    object Members : ScreenViewEvent("MembersFragment")
    object Mentions : ScreenViewEvent("MentionsFragment")
    object MessageInfo : ScreenViewEvent("MessageInfoFragment")
    object Password : ScreenViewEvent("PasswordFragment")
    object PinnedMessages : ScreenViewEvent("PinnedMessagesFragment")
    object Preferences : ScreenViewEvent("PreferencesFragment")
    object Profile : ScreenViewEvent("ProfileFragment")
    object RegisterUsername : ScreenViewEvent("RegisterUsernameFragment")
    object ResetPassword : ScreenViewEvent("ResetPasswordFragment")
    object Server : ScreenViewEvent("ServerFragment")
    object Settings : ScreenViewEvent("SettingsFragment")
    object SignUp : ScreenViewEvent("SignupFragment")
    object TwoFa : ScreenViewEvent("TwoFAFragment")
}
