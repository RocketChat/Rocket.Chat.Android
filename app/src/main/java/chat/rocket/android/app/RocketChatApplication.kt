package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Worker
import chat.rocket.android.BuildConfig
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.dagger.injector.HasWorkerInjector
import chat.rocket.android.dagger.qualifier.ForMessages
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.emoji.Fitzpatrick
import chat.rocket.android.emoji.internal.EmojiCategory
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.SITE_URL
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.android.util.setupFabric
import chat.rocket.common.RocketChatException
import chat.rocket.core.internal.rest.getCustomEmojis
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class RocketChatApplication : Application(), HasActivityInjector, HasServiceInjector,
    HasBroadcastReceiverInjector, HasWorkerInjector {

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var broadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var workerInjector: DispatchingAndroidInjector<Worker>

    @Inject
    lateinit var imagePipelineConfig: ImagePipelineConfig
    @Inject
    lateinit var draweeConfig: DraweeConfig

    // TODO - remove this from here when we have a proper service handling the connection.
    @Inject
    lateinit var getCurrentServerInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var settingsInteractor: GetSettingsInteractor
    @Inject
    lateinit var tokenRepository: TokenRepository
    @Inject
    lateinit var localRepository: LocalRepository
    @Inject
    lateinit var accountRepository: AccountsRepository
    @Inject
    lateinit var factory: RocketChatClientFactory

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

        context = WeakReference(applicationContext)

        AndroidThreeTen.init(this)

        setupFabric(this)
        setupFresco()
        setupTimber()

        if (localRepository.needOldMessagesCleanUp()) {
            messagesPrefs.edit {
                clear()
            }
            localRepository.setOldMessagesCleanedUp()
        }

        // TODO - remove REALM files.
        // TODO - remove this
        checkCurrentServer()

        // TODO - FIXME - we need to properly inject and initialize the EmojiRepository
        loadEmojis()
    }

    private fun checkCurrentServer() {
        val currentServer = getCurrentServerInteractor.get() ?: "<unknown>"

        if (currentServer == "<unknown>") {
            val message = "null currentServer"
            Timber.d(IllegalStateException(message), message)
        }

        val settings = settingsInteractor.get(currentServer)
        if (settings.isEmpty()) {
            val message = "Empty settings for: $currentServer"
            Timber.d(IllegalStateException(message), message)
        }
        val baseUrl = settings[SITE_URL]
        if (baseUrl == null) {
            val message = "Server $currentServer SITE_URL"
            Timber.d(IllegalStateException(message), message)
        }
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

    override fun activityInjector() = activityDispatchingAndroidInjector

    override fun serviceInjector() = serviceDispatchingAndroidInjector

    override fun broadcastReceiverInjector() = broadcastReceiverInjector

    override fun workerInjector() = workerInjector

    companion object {
        var context: WeakReference<Context>? = null
        fun getAppContext(): Context? {
            return context?.get()
        }
    }

    // TODO - FIXME - This is a big Workaround
    /**
     * Load all emojis for the current server. Simple emojis are always the same for every server,
     * but custom emojis vary according to the its url.
     */
    fun loadEmojis() {
        EmojiRepository.init(this)
        val currentServer = getCurrentServerInteractor.get()
        currentServer?.let { server ->
            GlobalScope.launch {
                val client = factory.get(server)
                EmojiRepository.setCurrentServerUrl(server)
                val customEmojiList = mutableListOf<Emoji>()
                try {
                    for (customEmoji in retryIO("getCustomEmojis()") { client.getCustomEmojis() }) {
                        customEmojiList.add(Emoji(
                                shortname = ":${customEmoji.name}:",
                                category = EmojiCategory.CUSTOM.name,
                                url = "$currentServer/emoji-custom/${customEmoji.name}.${customEmoji.extension}",
                                count = 0,
                                fitzpatrick = Fitzpatrick.Default.type,
                                keywords = customEmoji.aliases,
                                shortnameAlternates = customEmoji.aliases,
                                siblings = mutableListOf(),
                                unicode = "",
                                isDefault = true
                        ))
                    }

                    EmojiRepository.load(this@RocketChatApplication, customEmojis = customEmojiList)
                } catch (ex: RocketChatException) {
                    Timber.e(ex)
                    EmojiRepository.load(this@RocketChatApplication as Context)
                }
            }
        }
    }
}

private fun LocalRepository.needOldMessagesCleanUp() = getBoolean(CLEANUP_OLD_MESSAGES_NEEDED, true)
private fun LocalRepository.setOldMessagesCleanedUp() = save(CLEANUP_OLD_MESSAGES_NEEDED, false)

private const val CLEANUP_OLD_MESSAGES_NEEDED = "CLEANUP_OLD_MESSAGES_NEEDED"
