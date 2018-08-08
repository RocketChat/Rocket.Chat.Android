package chat.rocket.android.wallet.create.presentation

import android.content.Context

interface CreateWalletView {

    fun showWalletSuccessfullyCreatedMessage(mnemonic : String)

    fun showWalletCreationFailedMessage(error : String?)

    fun returnContext():Context?

    fun setMnemonic(mnemonic : String)

}