package chat.rocket.android.chatroom.adapter

import android.support.annotation.IntDef

const val PEOPLE = 0L
const val ROOMS = 1L

@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [PEOPLE, ROOMS])
annotation class AutoCompleteType
