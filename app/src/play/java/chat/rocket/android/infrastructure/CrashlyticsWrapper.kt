package chat.rocket.android.infrastructure

import android.app.Application
import chat.rocket.android.BuildConfig
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.SITE_URL
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.runBlocking

fun installCrashlyticsWrapper(
    context: Application,
    currentServerInteractor: GetCurrentServerInteractor,
    settingsInteractor: GetSettingsInteractor,
    accountRepository: AccountsRepository,
    localRepository: LocalRepository
) {
    if (isCrashlyticsEnabled()) {
        Thread.setDefaultUncaughtExceptionHandler(
            RocketChatUncaughtExceptionHandler(
                currentServerInteractor,
                settingsInteractor, accountRepository, localRepository
            )
        )
    }
}

private fun isCrashlyticsEnabled(): Boolean {
    return !BuildConfig.DEBUG
}

private class RocketChatUncaughtExceptionHandler(
    val currentServerInteractor: GetCurrentServerInteractor,
    val settingsInteractor: GetSettingsInteractor,
    val accountRepository: AccountsRepository,
    val localRepository: LocalRepository
) : Thread.UncaughtExceptionHandler {

    val crashlyticsHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        val currentServer = currentServerInteractor.get() ?: "<unknown>"
        Crashlytics.setString(KEY_CURRENT_SERVER, currentServer)
        runBlocking {
            val accounts = accountRepository.load()
            Crashlytics.setString(KEY_ACCOUNTS, accounts.toString())
        }

        val settings = settingsInteractor.get(currentServer)
        Crashlytics.setInt(KEY_SETTINGS_SIZE, settings.size)
        val baseUrl = settings[SITE_URL]?.toString()
        Crashlytics.setString(KEY_SETTINGS_BASE_URL, baseUrl)

        val user = localRepository.getCurrentUser(currentServer)
        Crashlytics.setString(KEY_CURRENT_USER, user?.toString())
        Crashlytics.setString(KEY_CURRENT_USERNAME, localRepository.username())

        if (crashlyticsHandler != null) {
            crashlyticsHandler.uncaughtException(t, e)
        } else {
            throw RuntimeException("Missing default exception handler")
        }
    }
}

private const val KEY_CURRENT_SERVER = "CURRENT_SERVER"
private const val KEY_CURRENT_USER = "CURRENT_USER"
private const val KEY_CURRENT_USERNAME = "CURRENT_USERNAME"
private const val KEY_ACCOUNTS = "ACCOUNTS"
private const val KEY_SETTINGS_SIZE = "SETTINGS_SIZE"
private const val KEY_SETTINGS_BASE_URL = "SETTINGS_BASE_URL"