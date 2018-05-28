package chat.rocket.android.app

/*import chat.rocket.android.app.migration.RealmMigration
import chat.rocket.android.app.migration.RocketChatLibraryModule
import chat.rocket.android.app.migration.RocketChatServerModule
import chat.rocket.android.app.migration.model.RealmBasedServerInfo
import chat.rocket.android.app.migration.model.RealmPublicSetting
import chat.rocket.android.app.migration.model.RealmSession
import chat.rocket.android.app.migration.model.RealmUser*/
import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ProcessLifecycleOwner
import chat.rocket.android.BuildConfig
import chat.rocket.android.authentication.domain.model.toToken
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.dagger.qualifier.ForMessages
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.common.model.Token
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.experimental.runBlocking
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class RocketChatApplication : Application(), HasActivityInjector, HasServiceInjector,
    HasBroadcastReceiverInjector {

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var broadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var imagePipelineConfig: ImagePipelineConfig
    @Inject
    lateinit var draweeConfig: DraweeConfig

    // TODO - remove this from here when we have a proper service handling the connection.
    @Inject
    lateinit var getCurrentServerInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var multiServerRepository: MultiServerTokenRepository
    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var tokenRepository: TokenRepository
    @Inject
    lateinit var prefs: SharedPreferences
    @Inject
    lateinit var getAccountsInteractor: GetAccountsInteractor
    @Inject
    lateinit var localRepository: LocalRepository

    @Inject
    @field:ForMessages
    lateinit var messagesPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(appLifecycleObserver)

        // TODO - remove this on the future, temporary migration stuff for pre-release versions.
        migrateInternalTokens()
        context = WeakReference(applicationContext)

        AndroidThreeTen.init(this)
        EmojiRepository.load(this)

        setupCrashlytics()
        setupFresco()
        setupTimber()

        if (localRepository.needOldMessagesCleanUp()) {
            messagesPrefs.edit {
                clear()
            }
            localRepository.setOldMessagesCleanedUp()
        }

        // TODO - remove REALM files.
    }

    private fun migrateInternalTokens() {
        if (!prefs.getBoolean(INTERNAL_TOKEN_MIGRATION_NEEDED, true)) {
            Timber.d("Tokens already migrated")
            return
        }

        getCurrentServerInteractor.get()?.let { serverUrl ->
            multiServerRepository.get(serverUrl)?.let { token ->
                tokenRepository.save(serverUrl, Token(token.userId, token.authToken))
            }
        }

        runBlocking {
            getAccountsInteractor.get().forEach { account ->
                multiServerRepository.get(account.serverUrl)?.let { token ->
                    tokenRepository.save(account.serverUrl, token.toToken())
                }
            }
        }

        prefs.edit { putBoolean(INTERNAL_TOKEN_MIGRATION_NEEDED, false) }
    }

    private fun setupCrashlytics() {
        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())
    }

    private fun setupFresco() {
        Fresco.initialize(this, imagePipelineConfig, draweeConfig)
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return activityDispatchingAndroidInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return serviceDispatchingAndroidInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return broadcastReceiverInjector
    }

    companion object {
        var context: WeakReference<Context>? = null
        fun getAppContext(): Context? {
            return context?.get()
        }
    }
}

private fun LocalRepository.needOldMessagesCleanUp() = getBoolean(CLEANUP_OLD_MESSAGES_NEEDED, true)
private fun LocalRepository.setOldMessagesCleanedUp() = save(CLEANUP_OLD_MESSAGES_NEEDED, false)

private const val INTERNAL_TOKEN_MIGRATION_NEEDED = "INTERNAL_TOKEN_MIGRATION_NEEDED"

private const val CLEANUP_OLD_MESSAGES_NEEDED = "CLEANUP_OLD_MESSAGES_NEEDED"