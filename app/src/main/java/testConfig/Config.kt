package testConfig

import chat.rocket.android.BuildConfig

class Config {
    companion object {
        const val SERVER: String = "open.rocket.chat"
        const val serverUrl: String = "serverUrl"

        //Organisation using RC forks should create a user with below details before running tests
        const val USERNAME: String = "user121"
        const val PASSWORD: String = "123456"
        const val NAME: String = "user121"
        const val EMAIL: String = "qasdf@gmail.com"

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
        const val VERSION_NAME: String = BuildConfig.VERSION_NAME
        const val VERSION_CODE: Int = BuildConfig.VERSION_CODE
        const val APP_VERSION: String = "Version: $VERSION_NAME ($VERSION_CODE)"


    }
}