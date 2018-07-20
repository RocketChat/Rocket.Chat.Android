package chat.rocket.android.chatinformation.presentation

import chat.rocket.android.chatinformation.viewmodel.ReadReceiptViewModel
import chat.rocket.android.core.behaviours.LoadingView

interface MessageInfoView : LoadingView {

    fun showGenericErrorMessage()

    fun showReadReceipts(messageReceipts: List<ReadReceiptViewModel>)
}
