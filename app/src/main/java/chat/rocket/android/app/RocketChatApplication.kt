package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import chat.rocket.android.BuildConfig
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.common.model.Token
import chat.rocket.core.TokenRepository
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import javax.inject.Inject

class RocketChatApplication : Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

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

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder().application(this).build().inject(this)

        // TODO - remove this when we have a proper service handling connection...
        initCurrentServer()

        AndroidThreeTen.init(this)
        EmojiRepository.load(this)

        setupCrashlytics()
        setupFresco()
        setupTimber()
    }

    // TODO - remove this when we have a proper service handling connection...
    private fun initCurrentServer() {
        val currentServer = getCurrentServerInteractor.get()
        val serverToken = currentServer?.let { multiServerRepository.get(currentServer) }
        val settings = currentServer?.let { settingsRepository.get(currentServer) }
        if (currentServer != null && serverToken != null && settings != null) {
            tokenRepository.save(Token(serverToken.userId, serverToken.authToken))
        }
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
}