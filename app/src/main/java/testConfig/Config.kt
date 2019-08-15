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
        const val TEST_USER: String ="dfcxc"
        const val TEST_USER2: String ="govind.dixit"

        //Non existing User
        const val NON_EXISTING_USER: String = "**33##&&"

        //Existing Channel
        // Other organisation have to create channels before testing
        const val TEST_CHANNEL: String ="general"
        const val TEST_CHANNEL2: String ="sandbox"
        const val TEST_CHANNEL3: String ="dfcxc"

        //Non existing Channel
        const val NON_EXISTING_CHANNEL: String = "**33##&&"

        const val CODE = "1234"
        const val USER_ID = "user_id"
        const val AUTH_TOKEN = "auth_token"
        const val USER_TOKEN = "user_token"

        const val CURRENT_SERVER: String = "https://$SERVER"
        const val COMMUNTIY_SERVER: String = CURRENT_SERVER
        const val DEFAULT_TEST_URL: String = CURRENT_SERVER
        const val USER_NAME: String = "userName"
        const val AVATAR_URL: String = "serverUrl/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val USER_AVATAR: String = "$CURRENT_SERVER/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val UPDATED_AVATAR: String = "$CURRENT_SERVER/avatar/$USERNAME?format=jpeg&rc_uid=null&rc_token=null"
        const val TERMS_OF_SERVICE: String = "Terms of Service"
        const val PRIVACY_POLICY: String = "Privacy Policy"
        const val TERMS_OF_SERVICE_URL: String = "$CURRENT_SERVER/terms-of-service"
        const val PRIVACY_POLICY_URL: String = "$CURRENT_SERVER/privacy-policy"
        const val ADMIN_PANEL_URL = "$CURRENT_SERVER/admin/info?layout=embedded"
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
        const val CHAT_ROOM_ID: String = "abcd1234ABCD"
        const val CHAT_ROOM_TYPE: String = "Public"
        const val OAUTH_TOKEN: String = "abcd1234ABCD"
        const val CAS_TOKEN: String = "abcd1234ABCD"
        const val SAML_TOKEN: String = "abcd1234ABCD"
        const val OAUTH_SECRET: String = "abcd1234ABCD"
        const val AUTHENTICATION_CODE: String = "abcd1234ABCD"
        const val TEST_MESSAGE: String = "This is a test message"
    }
}