package chat.rocket.android.helper

import android.app.Application
import android.os.AsyncTask
import android.security.KeyChain
import java.net.Socket
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.KeyManager
import chat.rocket.android.server.domain.GetClientCertInteractor
import chat.rocket.android.util.HttpLoggingInterceptor
import chat.rocket.android.util.BasicAuthenticatorInterceptor
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
import chat.rocket.android.util.ClientCertInterceptor


data class SslStuff(val privKey: PrivateKey, val certChain: Array<X509Certificate>)

class SslTask @Inject constructor(
        private val context: Application,
        private val getClientCertInteractor: GetClientCertInteractor,
        private val logger: HttpLoggingInterceptor,
        private val basicAuthenticator: BasicAuthenticatorInterceptor,
        private val clientCertHelper: ClientCertHelper
): AsyncTask<Void, Void, SslStuff>() {

    override fun doInBackground(vararg params: Void?): SslStuff {
        var alias = getClientCertInteractor.get()
        alias = alias.toString()
        val privKey = KeyChain.getPrivateKey(context.applicationContext, alias)
        val certChain = KeyChain.getCertificateChain(context.applicationContext, alias)

        return SslStuff(privKey, certChain)
    }

    override fun onPostExecute(result: SslStuff?) {
        var alias = getClientCertInteractor.get()
        if (result != null && !clientCertHelper.getSetSslSocket()) {
            alias = alias.toString()
            val (privateKey, certificates) = result
            val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
            val keyManager = object : X509ExtendedKeyManager() {
                override fun chooseClientAlias(strings: Array<String>, principals: Array<Principal>?, socket: Socket): String {
                    return alias
                }

                override fun chooseServerAlias(s: String, principals: Array<Principal>, socket: Socket): String {
                    return alias
                }

                override fun getCertificateChain(s: String): Array<X509Certificate>? {
                    return certificates
                }

                override fun getClientAliases(s: String, principals: Array<Principal>): Array<String> {
                    return arrayOf(alias)
                }

                override fun getServerAliases(s: String, principals: Array<Principal>): Array<String> {
                    return arrayOf(alias)
                }

                override fun getPrivateKey(s: String): PrivateKey? {
                    return privateKey
                }
            }

            val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustFactory.init(trustStore)

            val tm = arrayOf<X509TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return certificates
                }

                fun isClientTrusted(arg0: Array<X509Certificate>): Boolean {
                    return true
                }

                fun isServerTrusted(arg0: Array<X509Certificate>): Boolean {
                    return true
                }

            })

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(arrayOf<KeyManager>(keyManager), tm, null)
            SSLContext.setDefault(sslContext)

            if (!clientCertHelper.getSetSslSocket()) {
                clientCertHelper.setOkHttpClient(OkHttpClient.Builder()
                        .addInterceptor(logger)
                        .addInterceptor(basicAuthenticator)
                        .sslSocketFactory(sslContext.socketFactory)
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build())
            }
        }
    }
}


@Singleton
class ClientCertHelper @Inject constructor(
        private val context: Application,
        private val getClientCertInteractor: GetClientCertInteractor,
        private val logger: HttpLoggingInterceptor,
        private val basicAuthenticator: BasicAuthenticatorInterceptor
) {
    private val clientCert: ClientCertInterceptor = ClientCertInterceptor(this)
    private var setSslSocket: Boolean = false
    private var okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(clientCert)
            .addInterceptor(logger)
            .addInterceptor(basicAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

    fun getEnabled(): Boolean {
        var alias = getClientCertInteractor.get()
        return !alias.isNullOrEmpty()
    }

    fun getSetSslSocket(): Boolean {
        return this.setSslSocket
    }

    fun setOkHttpClient(client: OkHttpClient) {
        this.okHttpClient = client
        this.setSslSocket = true
    }

    fun getClient(): OkHttpClient {
        if (this.getEnabled() && !this.setSslSocket) {
            SslTask(context, getClientCertInteractor, logger, basicAuthenticator, this).execute()
        }
        return this.okHttpClient
    }
}
