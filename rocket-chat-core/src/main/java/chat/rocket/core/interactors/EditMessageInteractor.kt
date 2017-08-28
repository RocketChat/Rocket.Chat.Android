package chat.rocket.core.interactors

import chat.rocket.core.PermissionsConstants
import chat.rocket.core.PublicSettingsConstants
import chat.rocket.core.models.*
import chat.rocket.core.repositories.*
import chat.rocket.core.utils.Pair
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.functions.Function4

class EditMessageInteractor(private val permissionInteractor: PermissionInteractor,
                            private val userRepository: UserRepository,
                            private val messageRepository: MessageRepository,
                            private val roomRepository: RoomRepository,
                            private val publicSettingRepository: PublicSettingRepository) {

    fun isAllowed(message: Message): Single<Boolean> {
        return Single.zip<Optional<User>, Optional<Room>, Optional<PublicSetting>, Optional<PublicSetting>, Pair<Optional<Room>, Boolean>>(
                userRepository.getCurrent().first(Optional.absent()),
                roomRepository.getById(message.roomId).first(Optional.absent()),
                publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING),
                publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT),
                Function4 { user, room, allowEdit, editTimeout ->
                    val editAllowed = allowEdit.isPresent && allowEdit.get().valueAsBoolean

                    val editTimeLimitInMinutes = editTimeout.longValue()

                    val editAllowedInTime = if (editTimeLimitInMinutes > 0) {
                        message.timestamp.millisToMinutes() < editTimeLimitInMinutes
                    } else {
                        true
                    }

                    val editOwn = user.isPresent && user.get().id == message.user?.id

                    Pair.create(room, editAllowed && editAllowedInTime && editOwn)
                }
        )
                .flatMap { (room, editAllowed) ->
                    if (!room.isPresent) {
                        return@flatMap Single.just(false)
                    }

                    permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room.get())
                            .map { it || editAllowed }
                }
    }
}

fun Optional<PublicSetting>.longValue(defaultValue: Long = 0) = if (this.isPresent) {
    this.get().valueAsLong
} else {
    defaultValue
}

fun Long.millisToMinutes() = this / 60_000