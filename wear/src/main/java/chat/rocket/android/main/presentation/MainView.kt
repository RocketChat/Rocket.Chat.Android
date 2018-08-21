package chat.rocket.android.main.presentation

import chat.rocket.android.core.behaviour.MessagesView

interface MainView : MessagesView {

    /**
     * invalidate firebase push token
     * @param token Token for invalidating
     *
     */
    fun invalidateToken(token: String)
}