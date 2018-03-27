package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.SharedPreferences
import androidx.content.edit
import chat.rocket.android.BuildConfig
import chat.rocket.android.authentication.domain.model.toToken
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.server.domain.*
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.common.model.Token
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.experimental.runBlocking
import timber.log.Timber
import javax.inject.Inject
import android.content.BroadcastReceiver
import dagger.android.*


class RocketChatApplication : Application(), HasActivityInjector, HasServiceInjector,
        HasBroadcastReceiverInjector {

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

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder().application(this).build().inject(this)

        // TODO - remove this on the future, temporary migration stuff for pre-release versions.
        prefs.edit { putBoolean(INTERNAL_TOKEN_MIGRATION_NEEDED, true) }
        migrateInternalTokens()

        AndroidThreeTen.init(this)
        EmojiRepository.load(this)

        setupCrashlytics()
        setupFresco()
        setupTimber()
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
}

private const val INTERNAL_TOKEN_MIGRATION_NEEDED = "INTERNAL_TOKEN_MIGRATION_NEEDED"