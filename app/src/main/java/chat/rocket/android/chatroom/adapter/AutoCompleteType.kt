package chat.rocket.android.chatroom.adapter

import androidx.annotation.IntDef

const val PEOPLE = 0
const val ROOMS = 1

@Retention(AnnotationRetention.SOURCE)
@IntDef(PEOPLE, ROOMS)
annotation class AutoCompleteType
