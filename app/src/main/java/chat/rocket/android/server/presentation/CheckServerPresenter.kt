package chat.rocket.android.server.presentation

import chat.rocket.android.BuildConfig
import chat.rocket.android.authentication.server.presentation.VersionCheckView
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.VersionInfo
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatInvalidProtocolException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.serverInfo
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import timber.log.Timber

abstract class CheckServerPresenter constructor(private val strategy: CancelStrategy,
                                                private val factory: RocketChatClientFactory,
                                                private val view: VersionCheckView) {
    private lateinit var currentServer: String
    private lateinit var client: RocketChatClient

    internal fun checkServerInfo(serverUrl: String): Job {
        return launchUI(strategy) {
            try {
                currentServer = serverUrl
                client = factory.create(currentServer)
                val version = checkServerVersion(serverUrl).await()
                when (version) {
                    is Version.VersionOk -> {
                        Timber.i("Your version is nice! (Requires: 0.62.0, Yours: ${version.version})")
                        view.versionOk()
                    }
                    is Version.RecommendedVersionWarning -> {
                        Timber.i("Your server ${version.version} is bellow recommended version ${BuildConfig.RECOMMENDED_SERVER_VERSION}")
                        view.alertNotRecommendedVersion()
                    }
                    is Version.OutOfDateError -> {
                        Timber.i("Oops. Looks like your server ${version.version} is out-of-date! Minimum server version required ${BuildConfig.REQUIRED_SERVER_VERSION}!")
                        view.blockAndAlertNotRequiredVersion()
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error getting server info")
                when(ex) {
                    is RocketChatInvalidProtocolException -> {
                        view.errorInvalidProtocol()
                    }
                    else -> {
                        view.errorCheckingServerVersion()
                    }
                }
            }
        }
    }

    internal fun checkServerVersion(serverUrl: String): Deferred<Version> {
        currentServer = serverUrl
        return async {
            val serverInfo = retryIO(description = "serverInfo", times = 5) { client.serverInfo() }
            val thisServerVersion = serverInfo.version
            val isRequiredVersion = isRequiredServerVersion(thisServerVersion)
            val isRecommendedVersion = isRecommendedServerVersion(thisServerVersion)
            if (isRequiredVersion) {
                if (isRecommendedVersion) {
                    Timber.i("Your version is nice! (Requires: 0.62.0, Yours: $thisServerVersion)")
                    return@async Version.VersionOk(thisServerVersion)
                } else {
                    return@async Version.RecommendedVersionWarning(thisServerVersion)
                }
            } else {
                return@async Version.OutOfDateError(thisServerVersion)
            }
        }
    }

    private fun isRequiredServerVersion(version: String): Boolean {
        return isMinimumVersion(version, getVersionDistilled(BuildConfig.REQUIRED_SERVER_VERSION))
    }

    private fun isRecommendedServerVersion(version: String): Boolean {
        return isMinimumVersion(version, getVersionDistilled(BuildConfig.RECOMMENDED_SERVER_VERSION))
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
                full = version)
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
        data class RecommendedVersionWarning(private val currentVersion: String) : Version(currentVersion)
        data class OutOfDateError(private val currentVersion: String) : Version(currentVersion)
    }
}