package chat.rocket.android.files.presentation

import androidx.core.net.toUri
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.files.uimodel.FileUiModel
import chat.rocket.android.files.uimodel.FileUiModelMapper
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryDB
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.getFiles
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class FilesPresenter @Inject constructor(
    private val view: FilesView,
    private val strategy: CancelStrategy,
    private val dbManager: DatabaseManager,
    @Named("currentServer") private val currentServer: String,
    private val mapper: FileUiModelMapper,
    val factory: RocketChatClientFactory
) {
    private val client: RocketChatClient = factory.create(currentServer)
    private var offset: Int = 0

    /**
     * Load all files for the given room id.
     *
     * @param roomId The id of the room to get files from.
     */
    fun loadFiles(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                dbManager.getRoom(roomId)?.let {
                    val files = client.getFiles(roomId, roomTypeOf(it.chatRoom.type), offset)
                    val filesUiModel = mapper.mapToUiModelList(files.result)
                    view.showFiles(filesUiModel, files.total)
                    offset += 1 * 30
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server.")
                }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
                Timber.e(exception)
            } finally {
                view.hideLoading()
            }
        }
    }

    fun openFile(fileUiModel: FileUiModel) {
        when {
            fileUiModel.isImage -> fileUiModel.url?.let {
                view.openImage(it, fileUiModel.name ?: "")
            }
            fileUiModel.isMedia -> fileUiModel.url?.let {
                view.playMedia(it)
            }
            else -> fileUiModel.url?.let {
                view.openDocument(it.toUri())
            }
        }
    }
}