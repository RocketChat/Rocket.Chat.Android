package testConfig

import chat.rocket.android.BuildConfig

class Config {
    companion object {
        const val ORG_NAME: String = "Rocket.Chat"
        const val SERVER: String = "open.rocket.chat"
        const val SERVER_URL: String = "serverUrl"

        //Organisation using RC forks should create a user with below details before running tests
        const val USERNAME: String = "user121"
        const val PASSWORD: String = "123456"
        const val NAME: String = "user121"
        const val EMAIL: String = "qasdf@gmail.com"

        //Existing User
        const val EXISTING_USER: String ="dfcxc"
        const val EXISTING_USER2: String ="govind.dixit"

        //Non existing User
        const val NON_EXISTING_USER: String = "**33##&&"

        //Existing Channel
        const val EXISTING_CHANNEL: String ="general"
        const val SANDBOX: String ="sandbox"

        //Non existing Channel
        const val NON_EXISTING_CHANNEL: String = "**33##&&"

        const val CODE = "1234"
        const val USER_ID = "user_id"
        const val AUTH_TOKEN = "auth_token"

        const val currentServer: String = "https://$SERVER"
        const val communityServerUrl: String = currentServer
        const val defaultTestServer: String = currentServer
        const val userName: String = "userName"
        const val avatar: String = "serverUrl/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val userAvatar: String = "$currentServer/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val TERMS_OF_SERVICE: String = "Terms of Service"
        const val PRIVACY_POLICY: String = "Privacy Policy"
        const val termsOfServiceUrl: String = "$currentServer/terms-of-service"
        const val privacyPolicyUrl: String = "$currentServer/privacy-policy"
        const val ADMIN_PANEL_URL = "$currentServer/admin/info?layout=embedded"
        const val LICENSE_URL = "https://github.com/RocketChat/Rocket.Chat.Android/blob/develop/LICENSE"
        const val LICENSE = "LICENSE"
        const val CHANGE_STATUS: String = "CHANGE STATUS"
        const val ONLINE: String = "Online"
        const val BUSY: String = "Busy"
        const val AWAY: String = "Away"
        const val INVISIBLE: String = "Invisible"
        const val MEMBERS: String = "Members"
        const val CHANNELS: String = "Channels"
        const val DIRECT_MESSAGES: String = "Direct Messages"
        const val FILES: String = "Files"
        const val USERS: String = "Users"
        const val DIRECTORY: String = "Directory"
        const val VERSION_NAME: String = BuildConfig.VERSION_NAME
        const val VERSION_CODE: Int = BuildConfig.VERSION_CODE
        const val APP_VERSION: String = "Version: $VERSION_NAME ($VERSION_CODE)"
        const val FAVORITE_MESSAGES: String = "Favorite Messages"
        const val PINNED_MESSAGES: String = "Pinned Messages"
        const val MENTIONS: String = "Mentions"
        const val TEST_MESSAGE: String = "This is a test message"
    }
}