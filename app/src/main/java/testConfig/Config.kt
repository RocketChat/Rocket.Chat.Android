package testConfig

class Config {
    companion object {
        const val SERVER: String = "open.rocket.chat"
        const val serverUrl: String = "serverUrl"
        const val NAME: String = "user"
        const val EMAIL: String = "abc@gmail.com"

        //Organisation using RC forks should create a user with below details before running tests
        const val USERNAME: String = "user121"
        const val PASSWORD: String = "123456"

        const val CODE = "1234"
        const val USER_ID = "user_id"
        const val AUTH_TOKEN = "auth_token"

        const val communityServerUrl: String = "https://open.rocket.chat"
        const val currentServer: String = "https://open.rocket.chat"
        const val defaultTestServer: String = "https://open.rocket.chat"
        const val userName: String = "userName"
        const val avatar: String = "serverUrl/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val userAvatar: String = "$currentServer/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"
        const val TERMS_OF_SERVICE: String = "Terms of Service"
        const val PRIVACY_POLICY: String = "Privacy Policy"
        const val termsOfServiceUrl: String = "$currentServer/terms-of-service"
        const val privacyPolicyUrl: String = "$currentServer/privacy-policy"
    }
}