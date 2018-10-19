package chat.rocket.android.server.presentation

import chat.rocket.android.BuildConfig
import chat.rocket.android.authentication.server.presentation.VersionCheckView
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.OauthHelper
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.casLoginUrl
import chat.rocket.android.server.domain.gitlabUrl
import chat.rocket.android.server.domain.isCasAuthenticationEnabled
import chat.rocket.android.server.domain.isFacebookAuthenticationEnabled
import chat.rocket.android.server.domain.isGithubAuthenticationEnabled
import chat.rocket.android.server.domain.isGitlabAuthenticationEnabled
import chat.rocket.android.server.domain.isGoogleAuthenticationEnabled
import chat.rocket.android.server.domain.isLinkedinAuthenticationEnabled
import chat.rocket.android.server.domain.isLoginFormEnabled
import chat.rocket.android.server.domain.isRegistrationEnabledForNewUsers
import chat.rocket.android.server.domain.isWordpressAuthenticationEnabled
import chat.rocket.android.server.domain.wordpressUrl
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.VersionInfo
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.casUrl
import chat.rocket.android.util.extensions.generateRandomString
import chat.rocket.android.util.extensions.parseColor
import chat.rocket.android.util.extensions.samlUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatInvalidProtocolException
import chat.rocket.common.model.ServerInfo
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.serverInfo
import chat.rocket.core.internal.rest.settingsOauth
import kotlinx.coroutines.experimental.Job
import timber.log.Timber

private const val SERVICE_NAME_FACEBOOK = "facebook"
private const val SERVICE_NAME_GITHUB = "github"
private const val SERVICE_NAME_GOOGLE = "google"
private const val SERVICE_NAME_LINKEDIN = "linkedin"
private const val SERVICE_NAME_GILAB = "gitlab"
private const val SERVICE_NAME_WORDPRESS = "wordpress"


abstract class CheckServerPresenter constructor(
    private val strategy: CancelStrategy,
    private val factory: RocketChatClientFactory,
    private val settingsInteractor: GetSettingsInteractor? = null,
    private val view: VersionCheckView? = null
) {
    private lateinit var currentServer: String
    private lateinit var client: RocketChatClient
    private lateinit var settings: PublicSettings
    internal var state: String = ""
    internal var facebookOauthUrl: String? = null
    internal var githubOauthUrl: String? = null
    internal var googleOauthUrl: String? = null
    internal var linkedinOauthUrl: String? = null
    internal var gitlabOauthUrl: String? = null
    internal var wordpressOauthUrl: String? = null
    internal var casLoginUrl: String? = null
    internal var casToken: String? = null
    internal var customOauthUrl: String? = null
    internal var customOauthServiceName: String? = null
    internal var customOauthServiceNameTextColor: Int = 0
    internal var customOauthServiceButtonColor: Int = 0
    internal var samlUrl: String? = null
    internal var samlToken: String? = null
    internal var samlServiceName: String? = null
    internal var samlServiceNameTextColor: Int = 0
    internal var samlServiceButtonColor: Int = 0
    internal var totalSocialAccountsEnabled = 0
    internal var isLoginFormEnabled = false
    internal var isNewAccountCreationEnabled = false

    internal fun setupConnectionInfo(serverUrl: String) {
        settingsInteractor?.get(serverUrl)?.let {
            settings = it
        }
        client = factory.create(serverUrl)
    }

    internal fun checkServerInfo(serverUrl: String): Job {
        return launchUI(strategy) {
            try {
                currentServer = serverUrl
                val serverInfo = retryIO(description = "serverInfo", times = 5) {
                    client.serverInfo()
                }
                if (serverInfo.redirected) {
                    view?.updateServerUrl(serverInfo.url)
                }
                val version = checkServerVersion(serverInfo)
                when (version) {
                    is Version.VersionOk -> {
                        Timber.i("Your version is nice! (Requires: 0.62.0, Yours: ${version.version})")
                        view?.versionOk()
                    }
                    is Version.RecommendedVersionWarning -> {
                        Timber.i("Your server ${version.version} is bellow recommended version ${BuildConfig.RECOMMENDED_SERVER_VERSION}")
                        view?.alertNotRecommendedVersion()
                    }
                    is Version.OutOfDateError -> {
                        Timber.i("Oops. Looks like your server ${version.version} is out-of-date! Minimum server version required ${BuildConfig.REQUIRED_SERVER_VERSION}!")
                        view?.blockAndAlertNotRequiredVersion()
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error getting server info")
                when (ex) {
                    is RocketChatInvalidProtocolException -> view?.errorInvalidProtocol()
                    else -> view?.errorCheckingServerVersion()
                }
            }
        }
    }

    internal suspend fun checkEnabledAccounts(serverUrl: String) {
        try {
            val services = retryIO("settingsOauth()") {
                client.settingsOauth().services
            }

            if (services.isNotEmpty()) {
                state = OauthHelper.getState()
                checkEnabledOauthAccounts(services, serverUrl)
                checkEnabledCasAccounts(serverUrl)
                checkEnabledCustomOauthAccounts(services, serverUrl)
                checkEnabledSamlAccounts(services, serverUrl)
            }
        } catch (exception: RocketChatException) {
            Timber.e(exception)
        }
    }

    private fun checkEnabledOauthAccounts(services: List<Map<String,Any>>, serverUrl: String) {

        if (settings.isFacebookAuthenticationEnabled()) {
            getServiceMap(services, SERVICE_NAME_FACEBOOK)?.let { serviceMap ->
                getOauthClientId(serviceMap)?.let { clientId ->
                    facebookOauthUrl =
                            OauthHelper.getFacebookOauthUrl(clientId, serverUrl, state)
                    totalSocialAccountsEnabled++
                }
            }
        }

        if (settings.isGithubAuthenticationEnabled()) {
            getServiceMap(services, SERVICE_NAME_GITHUB)?.let { serviceMap ->
                getOauthClientId(serviceMap)?.let { clientId ->
                    githubOauthUrl =
                            OauthHelper.getGithubOauthUrl(clientId, state)
                    totalSocialAccountsEnabled++
                }
            }
        }

        if (settings.isGoogleAuthenticationEnabled()) {
            getServiceMap(services, SERVICE_NAME_GOOGLE)?.let { serviceMap ->
                getOauthClientId(serviceMap)?.let { clientId ->
                    googleOauthUrl =
                            OauthHelper.getGoogleOauthUrl(clientId, serverUrl, state)
                    totalSocialAccountsEnabled++
                }
            }
        }

        if (settings.isLinkedinAuthenticationEnabled()) {
            getServiceMap(services, SERVICE_NAME_LINKEDIN)?.let { serviceMap ->
                getOauthClientId(serviceMap)?.let { clientId ->
                    linkedinOauthUrl =
                            OauthHelper.getLinkedinOauthUrl(clientId, serverUrl, state)
                    totalSocialAccountsEnabled++
                }
            }
        }

        if (settings.isGitlabAuthenticationEnabled()) {
            getServiceMap(services, SERVICE_NAME_GILAB)?.let { serviceMap ->
                getOauthClientId(serviceMap)?.let { clientId ->
                    gitlabOauthUrl = if (settings.gitlabUrl() != null) {
                        OauthHelper.getGitlabOauthUrl(
                            host = settings.gitlabUrl(),
                            clientId = clientId,
                            serverUrl = serverUrl,
                            state = state
                        )
                    } else {
                        OauthHelper.getGitlabOauthUrl(
                            clientId = clientId,
                            serverUrl = serverUrl,
                            state = state
                        )
                    }
                    totalSocialAccountsEnabled++
                }
            }
        }

        if (settings.isWordpressAuthenticationEnabled()) {
            getServiceMap(services, SERVICE_NAME_WORDPRESS)?.let { serviceMap ->
                getOauthClientId(serviceMap)?.let { clientId ->
                    wordpressOauthUrl =
                            if (settings.wordpressUrl().isNullOrEmpty()) {
                                OauthHelper.getWordpressComOauthUrl(
                                    clientId,
                                    serverUrl,
                                    state
                                )
                            } else {
                                OauthHelper.getWordpressCustomOauthUrl(
                                    getCustomOauthHost(serviceMap)
                                        ?: "https://public-api.wordpress.com",
                                    getCustomOauthAuthorizePath(serviceMap)
                                        ?: "/oauth/authorize",
                                    clientId,
                                    serverUrl,
                                    SERVICE_NAME_WORDPRESS,
                                    state,
                                    getCustomOauthScope(serviceMap) ?: "openid"
                                )
                            }
                    totalSocialAccountsEnabled++
                }
            }
        }
    }

    private fun checkEnabledCasAccounts(serverUrl: String) {
        if (settings.isCasAuthenticationEnabled()) {
            casToken = generateRandomString(17)
            casLoginUrl = settings.casLoginUrl().casUrl(serverUrl, casToken.toString())
            totalSocialAccountsEnabled++
        }
    }

    private fun checkEnabledCustomOauthAccounts(services: List<Map<String,Any>>, serverUrl: String) {
        getCustomOauthServices(services).let {
            for (serviceMap in it) {
                customOauthServiceName = getCustomOauthServiceName(serviceMap)
                val host = getCustomOauthHost(serviceMap)
                val authorizePath = getCustomOauthAuthorizePath(serviceMap)
                val clientId = getOauthClientId(serviceMap)
                val scope = getCustomOauthScope(serviceMap)
                val serviceNameTextColor =
                    getServiceNameColorForCustomOauthOrSaml(serviceMap)
                val serviceButtonColor = getServiceButtonColor(serviceMap)
                if (customOauthServiceName != null &&
                    host != null &&
                    authorizePath != null &&
                    clientId != null &&
                    scope != null &&
                    serviceNameTextColor != null &&
                    serviceButtonColor != null
                ) {
                    customOauthUrl = OauthHelper.getCustomOauthUrl(
                        host,
                        authorizePath,
                        clientId,
                        serverUrl,
                        customOauthServiceName.toString(),
                        state,
                        scope
                    )
                    customOauthServiceNameTextColor = serviceNameTextColor
                    customOauthServiceButtonColor = serviceButtonColor
                    totalSocialAccountsEnabled++
                }
            }
        }
    }

    private fun checkEnabledSamlAccounts(services: List<Map<String,Any>>, serverUrl: String) {
        getSamlServices(services).let {
            samlToken = generateRandomString(17)
            for (serviceMap in it) {
                val provider = getSamlProvider(serviceMap)
                samlServiceName = getSamlServiceName(serviceMap)
                val serviceNameTextColor =
                    getServiceNameColorForCustomOauthOrSaml(serviceMap)
                val serviceButtonColor = getServiceButtonColor(serviceMap)

                if (provider != null &&
                    samlServiceName != null &&
                    serviceNameTextColor != null &&
                    serviceButtonColor != null
                ) {
                    samlUrl = serverUrl.samlUrl(provider, samlToken.toString())
                    samlServiceNameTextColor = serviceNameTextColor
                    samlServiceButtonColor = serviceButtonColor
                    totalSocialAccountsEnabled++
                }
            }
        }
    }

    internal fun checkIfLoginFormIsEnabled() {
        if (settings.isLoginFormEnabled()) {
            isLoginFormEnabled = true
        }
    }

    internal fun checkIfCreateNewAccountIsEnabled() {
        if (settings.isRegistrationEnabledForNewUsers() && settings.isLoginFormEnabled()) {
            isNewAccountCreationEnabled = true
        }
    }

    /** Returns an OAuth service map given a [serviceName].
     *
     * @param listMap The list of [Map] to get the service from.
     * @param serviceName The service name to get in the [listMap]
     * @return The OAuth service map or null otherwise.
     */
    private fun getServiceMap(
        listMap: List<Map<String, Any>>,
        serviceName: String
    ): Map<String, Any>? = listMap.find { map -> map.containsValue(serviceName) }

    /**
     * Returns the OAuth client ID of a [serviceMap].
     * REMARK: This function works for common OAuth providers (Google, Facebook, Github and so on)
     * as well as custom OAuth.
     *
     * @param serviceMap The service map to get the OAuth client ID.
     * @return The OAuth client ID or null otherwise.
     */
    private fun getOauthClientId(serviceMap: Map<String, Any>): String? =
        serviceMap["clientId"] as? String ?: serviceMap["appId"] as? String

    /**
     * Returns a custom OAuth service list.
     *
     * @return A custom OAuth service list, otherwise an empty list if there is no custom OAuth service.
     */
    private fun getCustomOauthServices(listMap: List<Map<String, Any>>): List<Map<String, Any>> =
        listMap.filter { map -> map["custom"] == true }

    /** Returns the custom OAuth service host.
     *
     * @param serviceMap The service map to get the custom OAuth service host.
     * @return The custom OAuth service host, otherwise null.
     */
    private fun getCustomOauthHost(serviceMap: Map<String, Any>): String? =
        serviceMap["serverURL"] as? String

    /** Returns the custom OAuth service authorize path.
     *
     * @param serviceMap The service map to get the custom OAuth service authorize path.
     * @return The custom OAuth service authorize path, otherwise null.
     */
    private fun getCustomOauthAuthorizePath(serviceMap: Map<String, Any>): String? =
        serviceMap["authorizePath"] as? String

    /** Returns the custom OAuth service scope.
     *
     * @param serviceMap The service map to get the custom OAuth service scope.
     * @return The custom OAuth service scope, otherwise null.
     */
    private fun getCustomOauthScope(serviceMap: Map<String, Any>): String? =
        serviceMap["scope"] as? String

    /** Returns the text of the custom OAuth service.
     *
     * @param serviceMap The service map to get the text of the custom OAuth service.
     * @return The text of the custom OAuth service, otherwise null.
     */
    private fun getCustomOauthServiceName(serviceMap: Map<String, Any>): String? =
        serviceMap["service"] as? String

    /**
     * Returns a SAML OAuth service list.
     *
     * @return A SAML service list, otherwise an empty list if there is no SAML OAuth service.
     */
    private fun getSamlServices(listMap: List<Map<String, Any>>): List<Map<String, Any>> =
        listMap.filter { map -> map["service"] == "saml" }

    /**
     * Returns the SAML provider.
     *
     * @param serviceMap The service map to provider from.
     * @return The SAML provider, otherwise null.
     */
    private fun getSamlProvider(serviceMap: Map<String, Any>): String? =
        (serviceMap["clientConfig"] as Map<*, *>)["provider"] as? String

    /**
     * Returns the text of the SAML service.
     *
     * @param serviceMap The service map to get the text of the SAML service.
     * @return The text of the SAML service, otherwise null.
     */
    private fun getSamlServiceName(serviceMap: Map<String, Any>): String? =
        serviceMap["buttonLabelText"] as? String

    /**
     * Returns the text color of the service name.
     * REMARK: This can be used for custom OAuth or SAML.
     *
     * @param serviceMap The service map to get the text color from.
     * @return The text color of the service (custom OAuth or SAML), otherwise null.
     */
    private fun getServiceNameColorForCustomOauthOrSaml(serviceMap: Map<String, Any>): Int? =
        (serviceMap["buttonLabelColor"] as? String)?.parseColor()

    /**
     * Returns the button color of the service name.
     * REMARK: This can be used for custom OAuth or SAML.
     *
     * @param serviceMap The service map to get the button color from.
     * @return The button color of the service (custom OAuth or SAML), otherwise null.
     */
    private fun getServiceButtonColor(serviceMap: Map<String, Any>): Int? =
        (serviceMap["buttonColor"] as? String)?.parseColor()


    private fun checkServerVersion(serverInfo: ServerInfo): Version {
        val thisServerVersion = serverInfo.version
        val isRequiredVersion = isRequiredServerVersion(thisServerVersion)
        val isRecommendedVersion = isRecommendedServerVersion(thisServerVersion)

        return if (isRequiredVersion) {
            if (isRecommendedVersion) {
                Timber.i("Your version is nice! (Requires: 0.62.0, Yours: $thisServerVersion)")
                Version.VersionOk(thisServerVersion)
            } else {
                Version.RecommendedVersionWarning(thisServerVersion)
            }
        } else {
            Version.OutOfDateError(thisServerVersion)
        }
    }

    private fun isRequiredServerVersion(version: String): Boolean {
        return isMinimumVersion(version, getVersionDistilled(BuildConfig.REQUIRED_SERVER_VERSION))
    }

    private fun isRecommendedServerVersion(version: String): Boolean {
        return isMinimumVersion(
            version,
            getVersionDistilled(BuildConfig.RECOMMENDED_SERVER_VERSION)
        )
    }

    private fun isMinimumVersion(version: String, required: VersionInfo): Boolean {
        val thisVersion = getVersionDistilled(version)
        with(thisVersion) {
            if (major < required.major) {
                return false
            } else if (major > required.major) {
                return true
            }
            if (minor < required.minor) {
                return false
            } else if (minor > required.minor) {
                return true
            }
            return update >= required.update
        }
    }

    private fun getVersionDistilled(version: String): VersionInfo {
        var split = version.split("-")
        if (split.isEmpty()) {
            return VersionInfo(0, 0, 0, null, "0.0.0")
        }
        val ver = split[0]
        var release: String? = null
        if (split.size > 1) {
            release = split[1]
        }
        split = ver.split(".")
        val major = getVersionNumber(split, 0)
        val minor = getVersionNumber(split, 1)
        val update = getVersionNumber(split, 2)

        return VersionInfo(
            major = major,
            minor = minor,
            update = update,
            release = release,
            full = version
        )
    }

    private fun getVersionNumber(split: List<String>, index: Int): Int {
        return try {
            split.getOrNull(index)?.toInt() ?: 0
        } catch (ex: NumberFormatException) {
            0
        }
    }

    sealed class Version(val version: String) {
        data class VersionOk(private val currentVersion: String) : Version(currentVersion)
        data class RecommendedVersionWarning(private val currentVersion: String) :
            Version(currentVersion)

        data class OutOfDateError(private val currentVersion: String) : Version(currentVersion)
    }
}