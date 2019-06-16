package chat.rocket.android

class UnitTestConfig {
    companion object {
        const val communityServerUrl: String = "https://open.rocket.chat"
        const val currentServer: String = "https://open.rocket.chat"
        const val validServer: String = "https://open.rocket.chat"
        const val invalidServer: String = "open.rocket.chat"
        const val serverUrl: String = "serverUrl"
        const val userName: String = "userName"
        const val avatar: String = "serverUrl/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val fullAvatar: String = "$currentServer/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val userAvatar: String = "$currentServer/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val TERMS_OF_SERVICE: String = "Terms of Service"
        const val PRIVACY_POLICY: String = "Privacy Policy"
        const val termsOfServiceUrl: String = "$currentServer/terms-of-service"
        const val privacyPolicyUrl: String = "$currentServer/privacy-policy"
    }
}