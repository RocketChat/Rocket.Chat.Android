package chat.rocket.android.wallet.presentation

import android.content.Context
import chat.rocket.android.chatrooms.infrastructure.ChatRoomsRepository
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.android.wallet.BlockchainInterface
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.Token
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import kotlinx.coroutines.experimental.newSingleThreadContext
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class WalletPresenter @Inject constructor (private val view: WalletView,
                                           private val strategy: CancelStrategy,
                                           private val navigator: MainNavigator,
                                           private val localRepository: LocalRepository,
                                           private val tokenRepository: TokenRepository,
                                           private val chatRoomsRepository: ChatRoomsRepository,
                                           serverInteractor: GetCurrentServerInteractor,
                                           factory: RocketChatClientFactory) {

    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private val restUrl: HttpUrl? = HttpUrl.parse(serverUrl)
    private val bcInterface = BlockchainInterface()
    private val runContext = newSingleThreadContext("wallet-presenter")


    /**
     * Get transaction history associated with the user's wallet
     */
    private fun loadTransactions(address: String) {
        launchUI(strategy) {
            try {
                // Query the DB for transaction hashes
                if (bcInterface.isValidAddress(address)) {
                    // TODO Here is where a call would be made to the backend that is indexing the
                    //      blockchain for a list of transaction hashes associated with the address
                    // val txHashList = backend call

                    // Then update the view with TransactionViewModels
                    // view.updateTransactions(bcInterface.getTransactions(address, txHashList))
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Check if the user has a wallet
     *  both tied to their rocket.chat account and stored on their device
     *  and display either their wallet or the create wallet button.
     * Only display the wallet if it is stored with the rocket.chat account and on
     *  the user's device.
     *  TODO add more options for checking for wallets (e.g. what if there's a private key file on the device, but no address in the rocket.chat account)
     */
    fun loadWallet(c: Context) {
        launchUI(strategy) {
            view.showLoading()
            try {
                loadWalletAddress {
                    if (bcInterface.isValidAddress(it) && bcInterface.walletFileExists(c, it)) {
                        view.showWallet(true, bcInterface.getBalance(it).toDouble())
                        loadTransactions(it)
                    } else {
                        view.showWallet(false)
                    }
                    view.hideLoading()
                }
            } catch (ex: Exception) {
                view.showWallet(false)
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
                    override fun onFailure(call: Call, e: IOException) { Timber.d("ERROR: request call failed!")}
                    override fun onResponse(call: Call, response: Response) {
                        var jsonObject = JSONObject(response.body()?.string())
                        var walletAddress = ""
                        if (jsonObject.isNull("error")) {

                            if (!jsonObject.isNull("user")) {
                                jsonObject = jsonObject.getJSONObject("user")

                                if (!jsonObject.isNull("customFields")) {
                                    jsonObject = jsonObject.getJSONObject("customFields")
                                    walletAddress = jsonObject.getString("walletAddress")
                                }
                            }
                        } else {
                            Timber.d("ERROR: %s", jsonObject.getString("error"))
                        }
                        launchUI(strategy) {
                            callback(walletAddress)
                        }
                    }
                })
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun getUserName(): String {
        return localRepository.get(LocalRepository.CURRENT_USERNAME_KEY) ?: ""
    }

    /**
     * Get all room names the user has open that are Direct Message rooms
     */
    fun loadDMRooms() {
        launchUI(strategy) {
            try {
                val allRoomsQuery = chatRoomsRepository.getChatRooms(ChatRoomsRepository.Order.ACTIVITY)
                allRoomsQuery.observeForever {
                    val rooms = it ?: emptyList()
                    view.setupSendToDialog(rooms.filter {
                        roomTypeOf(it.chatRoom.type) is RoomType.DirectMessage
                    }.map {
                        it.chatRoom.name
                    })
                }

            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Find an open direct message room that matches a given username
     *  and redirect the user to the ChatRoom Activity, then immediately to a Transaction Activity
     */
    fun loadDMRoomByName(name: String) {
        launchUI(strategy) {
            try {
                val allRoomsQuery = chatRoomsRepository.getChatRooms(ChatRoomsRepository.Order.ACTIVITY)
                allRoomsQuery.observeForever {
                    val rooms = it ?: emptyList()
                    rooms.forEach {room ->
                        if (room.chatRoom.name == name && roomTypeOf(room.chatRoom.type) is RoomType.DirectMessage) {
                            navigator.toChatRoom(
                                    chatRoomId =  room.chatRoom.id,
                                    chatRoomName = room.chatRoom.name,
                                    chatRoomType = room.chatRoom.type,
                                    isReadOnly = room.chatRoom.readonly ?: false,
                                    chatRoomLastSeen = room.chatRoom.lastSeen ?: -1,
                                    isSubscribed = room.chatRoom.open,
                                    isFromWallet = true,
                                    isCreator = true,
                                    isFavorite = room.chatRoom.favorite ?: false
                            )
                        }
                    }
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }
}