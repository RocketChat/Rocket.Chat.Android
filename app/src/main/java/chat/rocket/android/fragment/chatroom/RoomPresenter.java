package chat.rocket.android.fragment.chatroom

import android.support.v4.util.Pair

import com.hadisatrio.optional.Optional

import org.json.JSONException
import org.json.JSONObject

import chat.rocket.android.BackgroundLooper
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.RocketChatCache
import chat.rocket.android.api.MethodCallHelper
import chat.rocket.android.helper.AbsoluteUrlHelper
import chat.rocket.android.helper.LogIfError
import chat.rocket.android.helper.Logger
import chat.rocket.android.log.RCLog
import chat.rocket.android.service.ConnectivityManagerApi
import chat.rocket.android.shared.BasePresenter
import chat.rocket.core.SyncState
import chat.rocket.core.interactors.MessageInteractor
import chat.rocket.core.models.Message
import chat.rocket.core.models.Room
import chat.rocket.core.models.Settings
import chat.rocket.core.models.User
import chat.rocket.core.repositories.RoomRepository
import chat.rocket.core.repositories.UserRepository
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class RoomPresenter/* package */ internal constructor(private val roomId: String,
                                                      private val userRepository: UserRepository,
                                                      private val messageInteractor: MessageInteractor,
                                                      private val roomRepository: RoomRepository,
                                                      private val absoluteUrlHelper: AbsoluteUrlHelper,
                                                      private val methodCallHelper: MethodCallHelper,
                                                      private val connectivityManagerApi: ConnectivityManagerApi) : BasePresenter<RoomContract.View>(), RoomContract.Presenter {
    private var currentRoom: Room? = null

    private val roomUserPair: Single<Pair<Room, User>>
        get() = Single.zip(
                singleRoom,
                currentUser,
                BiFunction<Room, User, Pair<Room, User>> { first, second -> Pair(first, second) }
        )

    private val singleRoom: Single<Room>
        get() = roomRepository.getById(roomId)
                .filter(Predicate<Optional<Room>> { it.isPresent() })
                .map<Room>(Function<Optional<Room>, Room> { it.get() })
                .firstElement()
                .toSingle()

    private val currentUser: Single<User>
        get() = userRepository.getCurrent()
                .filter(Predicate<Optional<User>> { it.isPresent() })
                .map<User>(Function<Optional<User>, User> { it.get() })
                .firstElement()
                .toSingle()

    override fun bindView(view: RoomContract.View) {
        super.bindView(view)
        refreshRoom()
    }

    override fun refreshRoom() {
        getRoomRoles()
        getRoomInfo()
        getRoomHistoryStateInfo()
        getMessages()
        getUserPreferences()
    }

    override fun loadMessages() {
        val subscription = singleRoom
                .flatMap<Boolean>(Function<Room, SingleSource<out Boolean>> { messageInteractor.loadMessages(it) })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success ->
                            if (!success) {
                                connectivityManagerApi.keepAliveServer()
                            }
                        },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    override fun loadMoreMessages() {
        val subscription = singleRoom
                .flatMap<Boolean>(Function<Room, SingleSource<out Boolean>> { messageInteractor.loadMoreMessages(it) })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success ->
                            if (!success) {
                                connectivityManagerApi.keepAliveServer()
                            }
                        },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    override fun onMessageSelected(message: Message?) {
        if (message == null) {
            return
        }

        if (message.syncState == SyncState.DELETE_FAILED) {
            view.showMessageDeleteFailure(message)
        } else if (message.syncState == SyncState.FAILED) {
            view.showMessageSendFailure(message)
        } else if (message.type == null && message.syncState == SyncState.SYNCED) {
            // If message is not a system message show applicable actions.
            view.showMessageActions(message)
        }
    }

    override fun onMessageTap(message: Message?) {
        if (message == null) {
            return
        }

        if (message.syncState == SyncState.FAILED) {
            view.showMessageSendFailure(message)
        }
    }

    override fun replyMessage(message: Message, justQuote: Boolean) {
        val subscription = this.absoluteUrlHelper.rocketChatAbsoluteUrl
                .cache()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { serverUrl ->
                            if (serverUrl.isPresent) {
                                val absoluteUrl = serverUrl.get()
                                val baseUrl = absoluteUrl.baseUrl
                                view.onReply(absoluteUrl, buildReplyOrQuoteMarkdown(baseUrl, message, justQuote), message)
                            }
                        },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    override fun acceptMessageDeleteFailure(message: Message) {
        val subscription = messageInteractor.acceptDeleteFailure(message)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

        addSubscription(subscription)
    }

    override fun loadMissedMessages() {
        val appContext = RocketChatApplication.getInstance()
        val openedRooms = RocketChatCache(appContext).openedRooms
        if (openedRooms.has(roomId)) {
            try {
                val room = openedRooms.getJSONObject(roomId)
                val rid = room.optString("rid")
                val ls = room.optLong("ls")
                methodCallHelper.loadMissedMessages(rid, ls)
                        .continueWith(LogIfError())
            } catch (e: JSONException) {
                RCLog.e(e)
            }

        }
    }

    private fun buildReplyOrQuoteMarkdown(baseUrl: String, message: Message, justQuote: Boolean): String {
        if (currentRoom == null || message.user == null) {
            return ""
        }

        return if (currentRoom!!.isDirectMessage) {
            String.format("[ ](%s/direct/%s?msg=%s) ", baseUrl,
                    message.user!!.username,
                    message.id)
        } else {
            String.format("[ ](%s/channel/%s?msg=%s) %s", baseUrl,
                    currentRoom!!.name,
                    message.id,
                    if (justQuote) "" else "@" + message.user!!.username + " ")
        }
    }

    override fun sendMessage(messageText: String) {
        view.disableMessageInput()
        val subscription = roomUserPair
                .flatMap { pair -> messageInteractor.send(pair.first!!, pair.second!!, messageText) }
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success ->
                            if (success) {
                                view.onMessageSendSuccessfully()
                            }
                            view.enableMessageInput()
                        }
                ) { throwable ->
                    view.enableMessageInput()
                    Logger.report(throwable)
                }

        addSubscription(subscription)
    }

    override fun resendMessage(message: Message) {
        val subscription = currentUser
                .flatMap { user -> messageInteractor.resend(message, user) }
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

        addSubscription(subscription)
    }

    override fun updateMessage(message: Message, content: String) {
        view.disableMessageInput()
        val subscription = currentUser
                .flatMap { user -> messageInteractor.update(message, user, content) }
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success ->
                            if (success) {
                                view.onMessageSendSuccessfully()
                            }
                            view.enableMessageInput()
                        }
                ) { throwable ->
                    view.enableMessageInput()
                    Logger.report(throwable)
                }

        addSubscription(subscription)
    }

    override fun deleteMessage(message: Message) {
        val subscription = messageInteractor.delete(message)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

        addSubscription(subscription)
    }

    override fun onUnreadCount() {
        val subscription = roomUserPair
                .flatMap { roomUserPair ->
                    messageInteractor
                            .unreadCountFor(roomUserPair.first!!, roomUserPair.second!!)
                }
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { count -> view.showUnreadCount(count) },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    override fun onMarkAsRead() {
        val subscription = roomRepository.getById(roomId)
                .filter(Predicate<Optional<Room>> { it.isPresent() })
                .map<Room>(Function<Optional<Room>, Room> { it.get() })
                .firstElement()
                .filter(Predicate<Room> { it.isAlert() })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { room ->
                            methodCallHelper.readMessages(room.roomId)
                                    .continueWith(LogIfError())
                        },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    private fun getRoomRoles() {
        methodCallHelper.getRoomRoles(roomId)
    }

    private fun getRoomInfo() {
        val subscription = roomRepository.getById(roomId)
                .distinctUntilChanged()
                .filter(Predicate<Optional<Room>> { it.isPresent() })
                .map<Room>(Function<Optional<Room>, Room> { it.get() })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<Room> { this.processRoom(it) }, Consumer<Throwable> { Logger.report(it) })
        addSubscription(subscription)
    }

    private fun processRoom(room: Room) {
        this.currentRoom = room
        view.render(room)

        if (room.isDirectMessage) {
            getUserByUsername(room.name)
        }
    }

    private fun getUserByUsername(username: String) {
        val disposable = userRepository.getByUsername(username)
                .distinctUntilChanged()
                .filter(Predicate<Optional<User>> { it.isPresent() })
                .map<User>(Function<Optional<User>, User> { it.get() })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<User> { view.showUserStatus(it) }, Consumer<Throwable> { Logger.report(it) })
        addSubscription(disposable)
    }

    private fun getRoomHistoryStateInfo() {
        val subscription = roomRepository.getHistoryStateByRoomId(roomId)
                .distinctUntilChanged()
                .filter(Predicate<Optional<RoomHistoryState>> { it.isPresent() })
                .map<RoomHistoryState>(Function<Optional<RoomHistoryState>, RoomHistoryState> { it.get() })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { roomHistoryState ->
                            val syncState = roomHistoryState.getSyncState()
                            view.updateHistoryState(
                                    !roomHistoryState.isComplete(),
                                    syncState == SyncState.SYNCED || syncState == SyncState.FAILED
                            )
                        },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    private fun getMessages() {
        val subscription = Flowable.zip<Optional<Room>, Optional<RocketChatAbsoluteUrl>, Pair<Optional<Room>, Optional<RocketChatAbsoluteUrl>>>(roomRepository.getById(roomId),
                absoluteUrlHelper.rocketChatAbsoluteUrl.toFlowable().cache(), BiFunction<Optional<Room>, Optional<RocketChatAbsoluteUrl>, Pair<Optional<Room>, Optional<RocketChatAbsoluteUrl>>> { first, second -> Pair(first, second) })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .map<Optional<Room>> { pair ->
                    view.setupWith(pair.second!!.orNull())
                    pair.first
                }
                .filter(Predicate<Optional<Room>> { it.isPresent() })
                .map<Room>(Function<Optional<Room>, Room> { it.get() })
                .map { room ->
                    RocketChatCache(RocketChatApplication.getInstance())
                            .addOpenedRoom(room.roomId, room.lastSeen)
                    room
                }
                .flatMap<List<Message>>(Function<Room, Publisher<out List<Message>>> { messageInteractor.getAllFrom(it) })
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        Consumer<List<Message>> { view.showMessages(it) },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    private fun getUserPreferences() {
        val subscription = userRepository.getCurrent()
                .filter(Predicate<Optional<User>> { it.isPresent() })
                .map<User>(Function<Optional<User>, User> { it.get() })
                .filter { user -> user.settings != null }
                .map<Settings>(Function<User, Settings> { it.getSettings() })
                .filter { settings -> settings.preferences != null }
                .map<Preferences>(Function<Settings, Preferences> { it.getPreferences() })
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { preferences ->
                            if (preferences.isAutoImageLoad()) {
                                view.autoloadImages()
                            } else {
                                view.manualLoadImages()
                            }
                        },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }

    private fun getAbsoluteUrl() {
        val subscription = absoluteUrlHelper.rocketChatAbsoluteUrl
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { it -> view.setupWith(it.orNull()) },
                        Consumer<Throwable> { Logger.report(it) }
                )

        addSubscription(subscription)
    }
}
