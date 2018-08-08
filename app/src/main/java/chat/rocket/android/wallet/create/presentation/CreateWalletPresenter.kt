package chat.rocket.android.wallet.create.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.android.wallet.BlockchainInterface
import chat.rocket.common.model.Token
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import okhttp3.*
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CreateWalletPresenter @Inject constructor (private val view: CreateWalletView,
                                                 private val strategy: CancelStrategy,
                                                 private val tokenRepository: TokenRepository,
                                                 serverInteractor: GetCurrentServerInteractor,
                                                 factory: RocketChatClientFactory){

    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private val restUrl: HttpUrl? = HttpUrl.parse(serverUrl)
    private val bcInterface = BlockchainInterface()

    /**
     * Create a bip39 wallet (which uses mnemonic passphrases)
     *  and save the address to the user's account and display the passphrases to the user
     */
    fun createNewWallet(walletName: String, password: String){
        launchUI(strategy) {
            try {
                val response = bcInterface.createBip39Wallet(view.returnContext(), password)
                val address = response[0]
                val mnemonic =  response[1]
                updateWalletAddress(address, mnemonic)
            } catch (exception: Exception) {
                view.showWalletCreationFailedMessage(exception.message)
            }
        }

    }

    /**
     * Change the walletAddress field in the current user's customFields field
     *
     * @param address user's new wallet address
     *
     * NOTE: this function directly calls the REST API, which normally should be
     *          done in the Kotlin SDK
     */
    private fun updateWalletAddress(address: String, mnemonic: String) {
        launchUI(strategy) {
            try {
                val httpUrl = restUrl?.newBuilder()
                        ?.addPathSegment("api")
                        ?.addPathSegment("v1")
                        ?.addPathSegment("users.update")
                        ?.build()

                val me = retryIO("me") { client.me() }
                val payloadBody = "{\"userId\":\"${me.id}\",\"data\":{\"customFields\":{\"walletAddress\":\"$address\"}}}"
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payloadBody)
                val builder = Request.Builder().url(httpUrl)

                val token: Token? = tokenRepository.get(serverUrl)
                token?.let {
                    builder.addHeader("X-Auth-Token", token.authToken)
                            .addHeader("X-User-Id", token.userId)
                }
                val request = builder.post(body).build()

                val httpClient = OkHttpClient()
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) { Timber.d("ERROR: request call failed!")}
                    override fun onResponse(call: Call, response: Response) {

                        view.showWalletSuccessfullyCreatedMessage(mnemonic)
                    }
                })
            } catch (ex: Exception) {
                Timber.e(ex)
                view.showWalletCreationFailedMessage(ex.message)
            }
        }
    }
}