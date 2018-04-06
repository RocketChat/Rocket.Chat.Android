package chat.rocket.android.helper

import chat.rocket.android.util.extensions.removeTrailingSlash

object OauthHelper {

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
     * @param clientId The Gitlab client ID.
     * @param serverUrl The server URL.
     * @param state An unguessable random string used to protect against forgery attacks.
     * @return The Gitlab Oauth URL.
     */
    fun getGitlabOauthUrl(clientId: String, serverUrl: String, state: String): String {
        return  "https://gitlab.com/oauth/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=${serverUrl.removeTrailingSlash()}/_oauth/gitlab?close" +
                "&state=$state" +
                "&response_type=code" +
                "&scope=read_user"
    }
}