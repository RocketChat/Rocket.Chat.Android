package chat.rocket.android.emoji.internal

import chat.rocket.android.emoji.Emoji

fun Emoji.isCustom(): Boolean = this.url != null
