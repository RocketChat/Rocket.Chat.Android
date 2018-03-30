package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import androidx.content.edit
import chat.rocket.android.BuildConfig
import chat.rocket.android.app.migration.RealmMigration
import chat.rocket.android.app.migration.RocketChatLibraryModule
import chat.rocket.android.app.migration.RocketChatServerModule
import chat.rocket.android.app.migration.model.RealmBasedServerInfo
import chat.rocket.android.app.migration.model.RealmPublicSetting
import chat.rocket.android.app.migration.model.RealmSession
import chat.rocket.android.app.migration.model.RealmUser
import chat.rocket.android.authentication.domain.model.toToken
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.common.model.Token
import chat.rocket.core.model.Value
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.*
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import timber.log.Timber
import javax.inject.Inject


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
    lateinit var accountRepository: AccountsRepository
    @Inject
    lateinit var saveCurrentServerRepository: SaveCurrentServerInteractor
    @Inject
    lateinit var prefs: SharedPreferences
    @Inject
    lateinit var getAccountsInteractor: GetAccountsInteractor
    @Inject
    lateinit var localRepository: LocalRepository

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder().application(this).build().inject(this)

        // TODO - remove this on the future, temporary migration stuff for pre-release versions.
        migrateInternalTokens()

        AndroidThreeTen.init(this)
        EmojiRepository.load(this)

        setupCrashlytics()
        setupFresco()
        setupTimber()

        // TODO - remove this and all realm stuff when we got to 80% in 2.0
        try {
            if (!localRepository.hasMigrated()) {
                migrateFromLegacy()
            }
        } catch (ex: Exception) {
            Timber.d(ex, "Error migrating old accounts")
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
        val avatar = UrlHelper.getAvatarUrl(url, user.username!!)
        val icon = settings.favicon()?.let {
            UrlHelper.getServerLogoUrl(url, it)
        }
        val logo = settings.wideTile()?.let {
            UrlHelper.getServerLogoUrl(url, it)
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

private fun LocalRepository.setMigrated(migrated: Boolean) {
    save(LocalRepository.MIGRATION_FINISHED_KEY, migrated)
}

private fun LocalRepository.hasMigrated() = getBoolean(LocalRepository.MIGRATION_FINISHED_KEY)

private const val INTERNAL_TOKEN_MIGRATION_NEEDED = "INTERNAL_TOKEN_MIGRATION_NEEDED"