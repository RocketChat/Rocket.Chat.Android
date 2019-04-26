package chat.rocket.android.analytics.event

sealed class ScreenViewEvent(val screenName: String) {

    // Authentication
    object OnBoarding : ScreenViewEvent("OnBoardingFragment")
    object Server : ScreenViewEvent("ServerFragment")
    object LoginOptions : ScreenViewEvent("LoginOptionsFragment")
    object Login : ScreenViewEvent("LoginFragment")
    object TwoFa : ScreenViewEvent("TwoFAFragment")
    object SignUp : ScreenViewEvent("SignupFragment")
    object RegisterUsername : ScreenViewEvent("RegisterUsernameFragment")
    object ResetPassword : ScreenViewEvent("ResetPasswordFragment")

    object About : ScreenViewEvent("AboutFragment")
    object ChatRoom : ScreenViewEvent("ChatRoomFragment")
    object ChatRooms : ScreenViewEvent("ChatRoomsFragment")
    object CreateChannel : ScreenViewEvent("CreateChannelFragment")
    object UserDetails : ScreenViewEvent("UserDetailsFragment")
    object FavoriteMessages : ScreenViewEvent("FavoriteMessagesFragment")
    object Files : ScreenViewEvent("FilesFragment")
    object Members : ScreenViewEvent("MembersFragment")
    object Mentions : ScreenViewEvent("MentionsFragment")
    object MessageInfo : ScreenViewEvent("MessageInfoFragment")
    object Password : ScreenViewEvent("PasswordFragment")
    object PinnedMessages : ScreenViewEvent("PinnedMessagesFragment")
    object Preferences : ScreenViewEvent("PreferencesFragment")
    object Profile : ScreenViewEvent("ProfileFragment")
    object Settings : ScreenViewEvent("SettingsFragment")
    object Directory : ScreenViewEvent("DirectoryFragment")
}
