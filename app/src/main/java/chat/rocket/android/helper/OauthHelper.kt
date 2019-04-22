package chat.rocket.android.helper

import chat.rocket.android.util.extensions.encodeToBase64
import chat.rocket.android.util.extensions.generateRandomString
import chat.rocket.android.util.extensions.removeTrailingSlash

object OauthHelper {

    /**
     * Returns an unguessable random string used to protect against forgery attacks.
     */
    fun getState() =
        ("{\"loginStyle\":\"popup\"," +
                "\"credentialToken\":\"${generateRandomString(40)}\"," +
                "\"isCordova\":true}").encodeToBase64()

    /**
     * Returns the Github Oauth URL.
     *
     * @param clientId The GitHub client ID.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The Github Oauth URL.
     */
    fun getGithubOauthUrl(clientId: String, state: String): String {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=$clientId" +
                "&state=$state" +
                "&scope=user:email"
    }

    /**
     * Returns the Google Oauth URL.
     *
     * @param clientId The Google client ID.
     * @param serverUrl The server URL.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The Google Oauth URL.
     */
    fun getGoogleOauthUrl(clientId: String, serverUrl: String, state: String): String {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/google?close" +
                "&state=$state" +
                "&response_type=code" +
                "&scope=email%20profile"
    }

    /**
     * Returns the Linkedin Oauth URL.
     *
     * @param clientId The Linkedin client ID.
     * @param serverUrl The server URL.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The Linkedin Oauth URL.
     */
    fun getLinkedinOauthUrl(clientId: String, serverUrl: String, state: String): String {
        return "https://linkedin.com/oauth/v2/authorization" +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/linkedin?close" +
                "&state=$state" +
                "&response_type=code"
    }

    /**
     * Returns the Gitlab Oauth URL.
     *
     * @param host The Gitlab host.
     * @param clientId The Gitlab client ID.
     * @param serverUrl The server URL.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The Gitlab Oauth URL.
     */
    fun getGitlabOauthUrl(
        host: String? = "https://gitlab.com",
        clientId: String,
        serverUrl: String,
        state: String
    ): String {
        return host +
                "/oauth/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/gitlab?close" +
                "&state=$state" +
                "&response_type=code" +
                "&scope=read_user"
    }

    /**
     * Returns the Facebook Oauth URL.
     *
     * @param clientId The Facebook client ID.
     * @param serverUrl The server URL.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The Facebook Oauth URL.
     */
    fun getFacebookOauthUrl(clientId: String, serverUrl: String, state: String): String {
        return "https://facebook.com/v2.9/dialog/oauth" +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/facebook?close" +
                "&state=$state" +
                "&response_type=code" +
                "&scope=email"
    }

    /**
     * Returns the WordPress-Com Oauth URL.
     *
     * @param clientId The WordPress-Com client ID.
     * @param serverUrl The server URL.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The WordPress-Com Oauth URL.
     */
    fun getWordpressComOauthUrl(clientId: String, serverUrl: String, state: String): String {
        return "https://public-api.wordpress.com/oauth2/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/wordpress?close" +
                "&state=$state" +
                "&response_type=code" +
                "&scope=auth"
    }

    /**
     * Returns the WordPress custom Oauth URL.
     *
     * @param host The WordPress custom OAuth host.
     * @param authorizePath The WordPress custom OAuth authorization path.
     * @param clientId The WordPress custom OAuth client ID.
     * @param serverUrl The server URL.
     * @param serviceName The service name.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @param scope The WordPress custom OAuth scope.
     * @return The WordPress custom Oauth URL.
     */
    fun getWordpressCustomOauthUrl(
        host: String,
        authorizePath: String,
        clientId: String,
        serverUrl: String,
        serviceName: String,
        state: String,
        scope: String
    ): String {
        (authorizePath +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/$serviceName?close" +
                "&state=$state" +
                "&scope=$scope" +
                "&response_type=code"
                ).let {
            return if (it.contains(host)) {
                it
            } else {
                host + it
            }
        }
    }

    /**
     * Returns the Custom Oauth URL.
     *
     * @param host The custom OAuth host.
     * @param authorizePath The OAuth authorization path.
     * @param clientId The custom OAuth client ID.
     * @param serverUrl The server URL.
     * @param serviceName The service name.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @param scope The custom OAuth scope.
     * @return The Custom Oauth URL.
     */
    fun getCustomOauthUrl(
        host: String,
        authorizePath: String,
        clientId: String,
        serverUrl: String,
        serviceName: String,
        state: String,
        scope: String
    ): String {
        (authorizePath +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/$serviceName" +
                "&state=$state" +
                "&scope=$scope" +
                "&response_type=code"
                ).let {
            return if (it.contains(host)) {
                it
            } else {
                host + it
            }
        }
    }
}