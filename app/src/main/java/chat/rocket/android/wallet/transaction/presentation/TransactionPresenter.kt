package chat.rocket.android.wallet.transaction.presentation

import android.content.Context
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
import kotlinx.coroutines.experimental.async
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class TransactionPresenter @Inject constructor (private val view: TransactionView,
                                                private val strategy: CancelStrategy,
                                                private val tokenRepository: TokenRepository,
                                                serverInteractor: GetCurrentServerInteractor,
                                                factory: RocketChatClientFactory) {

    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private val restUrl: HttpUrl? = HttpUrl.parse(serverUrl)
    private val bcInterface = BlockchainInterface()

    /**
     * Send a transaction on the blockchain
     *
     * @param password the sender's password to unlock his/her private key file
     * @param senderAddr wallet address of the sender
     * @param recipientAddr wallet address of the recipient
     * @param amount Double amount of ether being sent
     * @param c Context/Activity
     */
    fun sendTransaction(password: String, senderAddr: String, recipientAddr: String, amount: Double, c: Context, reason: String) {
        launchUI(strategy) {
            view.showLoading()
            async {
                try {
                    val txHash = bcInterface.sendTransaction(c, password, senderAddr, recipientAddr, amount)

                    // TODO add a separate message/notification for when the transaction is actually completed/mined
                    view.showSuccessfulTransaction(amount, txHash, reason)
                } catch (ex: Exception) {
                    view.hideLoading()
                    view.showTransactionFailedMessage(ex.message)
                    Timber.e(ex)
                }
            }
        }
    }

    /**
     * Fetch the current user's wallet balance, using the wallet address stored
     *  in their CustomFields
     */
    fun loadUserTokens() {
        launchUI(strategy) {
            view.showLoading()
            try {
                val me = retryIO("me") { client.me() }
                loadWalletAddress(me.username) {
                    if (it.isEmpty()) {
                        view.showNoWalletError()
                    } else {
                        view.showUserWallet(it, bcInterface.getBalance(it))
                    }
                    view.hideLoading()
                }
            } catch (ex: Exception) {
                view.hideLoading()
                Timber.e(ex)
            }
        }
    }

    /**
     * Retrieve the walletAddress field in a user's customFields object
     *
     * If the user does not have a wallet address stored, then an empty string
     *  is given to the callback
     *
     * @param username the user name of the user to get the walletAddress of
     *                  if none is given, then the current user is used
     *
     * NOTE: this function directly calls the REST API, which normally should be
     *          done in the Kotlin SDK
     */
    fun loadWalletAddress(username: String? = null, callback: (String) -> Unit) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val me = retryIO("me") { client.me() }
                val httpUrl = restUrl?.newBuilder()
                        ?.addPathSegment("api")
                        ?.addPathSegment("v1")
                        ?.addPathSegment("users.info")
                        ?.addQueryParameter("username", username ?: me.username)
                        ?.build()

                val token: Token? = tokenRepository.get(serverUrl)
                val builder = Request.Builder().url(httpUrl)
                token?.let {
                    builder.addHeader("X-Auth-Token", token.authToken)
                            .addHeader("X-User-Id", token.userId)
                }

                val request = builder.get().build()
                val httpClient = OkHttpClient()
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Timber.d("ERROR: request call failed!")
                        launchUI(strategy) {
                            view.hideLoading()
                            callback("")
                        }
                    }
                    override fun onResponse(call: Call, response: Response) {
                        var jsonObject = JSONObject(response.body()?.string())
                        var walletAddress = ""
                        if (jsonObject.isNull("error")) {

                            if (!jsonObject.isNull("user")) {
                                jsonObject = jsonObject.getJSONObject("user")

                                if (!jsonObject.isNull("customFields")) {
                                    jsonObject = jsonObject.getJSONObject("customFields")
                                    walletAddress = jsonObject.getString("walletAddress")
                                    if (!bcInterface.isValidAddress(walletAddress)) {
                                        walletAddress = ""
                                    }
                                }
                            }
                        } else {
                            Timber.d("ERROR: %s", jsonObject.getString("error"))
                        }
                        launchUI(strategy) {
                            view.hideLoading()
                            callback(walletAddress)
                        }
                    }
                })
            } catch (ex: Exception) {
                view.hideLoading()
                Timber.e(ex)
            }
        }
    }

}