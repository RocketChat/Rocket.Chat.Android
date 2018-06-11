package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import chat.rocket.android.BuildConfig
import chat.rocket.android.app.migration.RealmMigration
import chat.rocket.android.app.migration.RocketChatLibraryModule
import chat.rocket.android.app.migration.RocketChatServerModule
import chat.rocket.android.app.migration.model.RealmBasedServerInfo
import chat.rocket.android.app.migration.model.RealmPublicSetting
import chat.rocket.android.app.migration.model.RealmSession
import chat.rocket.android.app.migration.model.RealmUser
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.dagger.qualifier.ForMessages
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.infrastructure.CrashlyticsWrapper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.installCrashlyticsWrapper
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SITE_URL
import chat.rocket.android.server.domain.SaveCurrentServerInteractor
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.domain.wideTile
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.common.model.Token
import chat.rocket.core.model.Value
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
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
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
    lateinit var settingsInteractor: GetSettingsInteractor
    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var tokenRepository: TokenRepository
    @Inject
    lateinit var accountRepository: AccountsRepository
    @Inject
    lateinit var saveCurrentServerRepository: SaveCurrentServerInteractor
    @Inject
    lateinit var prefs: SharedPreferences
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

        // TODO - remove this and all realm stuff when we got to 80% in 2.0
        try {
            if (!localRepository.hasMigrated()) {
                migrateFromLegacy()
            }
        } catch (ex: Exception) {
            Timber.d(ex, "Error migrating old accounts")
        }

        // TODO - remove this
        checkCurrentServer()
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

    private fun migrateFromLegacy() {
        Realm.init(this)
        val serveListConfiguration = RealmConfiguration.Builder()
                .name("server.list.realm")
                .schemaVersion(6)
                .migration(RealmMigration())
                .modules(RocketChatServerModule())
                .build()

        val serverRealm = Realm.getInstance(serveListConfiguration)
        val serversInfoList = serverRealm.where(RealmBasedServerInfo::class.java).findAll().toList()
        serversInfoList.forEach { server ->
            val hostname = server.hostname
            val url = if (server.insecure) "http://$hostname" else "https://$hostname"

            val config = RealmConfiguration.Builder()
                    .name("${server.hostname}.realm")
                    .schemaVersion(6)
                    .migration(RealmMigration())
                    .modules(RocketChatLibraryModule())
                    .build()

            val realm = Realm.getInstance(config)
            val user = realm.where(RealmUser::class.java)
                    .isNotEmpty(RealmUser.EMAILS).findFirst()
            val session = realm.where(RealmSession::class.java).findFirst()

            migratePublicSettings(url, realm)
            if (user != null && session != null) {
                val authToken = session.token
                settingsRepository.get(url)
                migrateServerInfo(url, authToken!!, settingsRepository.get(url), user)
            }
            realm.close()
        }
        migrateCurrentServer(serversInfoList)
        serverRealm.close()
        localRepository.setMigrated(true)
    }

    private fun migrateServerInfo(url: String, authToken: String, settings: PublicSettings, user: RealmUser) {
        val userId = user._id
        val avatar = url.avatarUrl(user.username!!)
        val icon = settings.favicon()?.let {
            url.serverLogoUrl(it)
        }
        val logo = settings.wideTile()?.let {
            url.serverLogoUrl(it)
        }
        val account = Account(url, icon, logo, user.username!!, avatar)
        launch(CommonPool) {
            tokenRepository.save(url, Token(userId!!, authToken))
            accountRepository.save(account)
        }
    }

    private fun migratePublicSettings(url: String, realm: Realm) {
        val settings = realm.where(RealmPublicSetting::class.java).findAll()

        val serverSettings = hashMapOf<String, Value<Any>>()
        settings.toList().forEach { setting ->
            val type = setting.type!!
            val value = setting.value!!

            val convertedSetting = when (type) {
                "string" -> Value(value)
                "language" -> Value(value)
                "boolean" -> Value(value.toBoolean())
                "int" -> try {
                    Value(value.toInt())
                } catch (ex: NumberFormatException) {
                    Value(0)
                }
                else -> null // ignore
            }

            if (convertedSetting != null) {
                val id = setting._id!!
                serverSettings.put(id, convertedSetting)
            }
        }
        settingsRepository.save(url, serverSettings)
    }

    private fun migrateCurrentServer(serversList: List<RealmBasedServerInfo>) {
        if (getCurrentServerInteractor.get() == null) {
            var currentServer = getSharedPreferences("cache", Context.MODE_PRIVATE)
                    .getString("KEY_SELECTED_SERVER_HOSTNAME", null)

            currentServer = if (serversList.isNotEmpty()) {
                val server = serversList.find { it.hostname == currentServer }
                val hostname = server!!.hostname
                if (server.insecure) {
                    "http://$hostname"
                } else {
                    "https://$hostname"
                }
            } else {
                "http://$currentServer"
            }
            saveCurrentServerRepository.save(currentServer)
        }
    }

    private fun setupCrashlytics() {
        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())

        installCrashlyticsWrapper(this@RocketChatApplication,
                getCurrentServerInteractor, settingsInteractor,
                accountRepository, localRepository)
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

private fun LocalRepository.setMigrated(migrated: Boolean) {
    save(LocalRepository.MIGRATION_FINISHED_KEY, migrated)
}

private fun LocalRepository.hasMigrated() = getBoolean(LocalRepository.MIGRATION_FINISHED_KEY)
private fun LocalRepository.needOldMessagesCleanUp() = getBoolean(CLEANUP_OLD_MESSAGES_NEEDED, true)
private fun LocalRepository.setOldMessagesCleanedUp() = save(CLEANUP_OLD_MESSAGES_NEEDED, false)

private const val CLEANUP_OLD_MESSAGES_NEEDED = "CLEANUP_OLD_MESSAGES_NEEDED"