package chat.rocket.android.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import chat.rocket.android.BuildConfig
import chat.rocket.android.app.migration.RealmMigration
import chat.rocket.android.app.migration.RocketChatLibraryModule
import chat.rocket.android.app.migration.RocketChatServerModule
import chat.rocket.android.app.migration.model.RealmBasedServerInfo
import chat.rocket.android.app.migration.model.RealmPublicSetting
import chat.rocket.android.app.migration.model.RealmUser
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.helper.CrashlyticsTree
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.common.model.Token
import chat.rocket.core.TokenRepository
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
import dagger.android.HasServiceInjector
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
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
        setupMigration()
    }

    private fun setupMigration() {
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
            val authToken = server.session
            val url = if (server.insecure) "http://$hostname" else "https://$hostname"
            val serverLogo = "$url/images/logo/android-chrome-192x192.png"
            val serverBg = "$url/images/logo/mstile-310x150.png"

            val config = RealmConfiguration.Builder()
                    .name("${server.hostname}.realm")
                    .schemaVersion(6)
                    .migration(RealmMigration())
                    .modules(RocketChatLibraryModule())
                    .build()

            val realm = Realm.getInstance(config)
            val user = realm.where(RealmUser::class.java)
                    .isNotEmpty(RealmUser.EMAILS).findFirst()

            if (user != null) {
                migrateServerInfo(url, authToken!!, serverLogo, serverBg, user)
            }
            migratePublicSettings(url, realm)

            realm.close()
        }
        migrateCurrentServer(serversInfoList)
        serverRealm.close()
    }

    private fun migrateServerInfo(url: String, authToken: String, serverLogo: String, serverBg: String, user: RealmUser) {
        val userId = user._id
        val avatar = "$url/avatar/${user.username}?format=jpeg"

        println("token_$url: ")
        println("{\n" +
                "   userId: $userId,\n" +
                "   authToken: $authToken\n" +
                "}")

        println("ACCOUNT_KEY: ")
        println("{\n" +
                "   serverUrl: $url,\n" +
                "   avatar: $avatar,\n" +
                "   serverLogo: $serverLogo,\n" +
                "   serverBg: $serverBg\n" +
                "}")
    }

    private fun migratePublicSettings(url: String, realm: Realm) {
        println("settings_$url: ")
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
                serverSettings.put(setting._id!!, convertedSetting)
                println("$id: $convertedSetting")
            }
        }
    }

    private fun migrateCurrentServer(serversList: List<RealmBasedServerInfo>) {
        var currentServer = getSharedPreferences("cache", Context.MODE_PRIVATE)
                .getString("KEY_SELECTED_SERVER_HOSTNAME", null)

        currentServer = if (serversList.toList().isNotEmpty()) {
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

        println("current_server: $currentServer")
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