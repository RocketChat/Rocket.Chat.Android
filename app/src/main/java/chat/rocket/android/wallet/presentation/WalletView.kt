package chat.rocket.android.wallet.presentation

import chat.rocket.android.wallet.ui.TransactionViewModel

interface WalletView {

    /**
     * Retrieve an updated wallet balance from the backend and display it
     */
    fun showBalance(bal: Double)

    /**
     * Switch between displaying the wallet UI (balance, send button, transaction history, etc)
     *  and displaying the UI for creating a new wallet
     */
    fun showWallet(value: Boolean = true, bal: Double = -1.0)

    /**
     * When trying to send tokens through a direct message room that doesn't exist,
     *  keep the user in the WalletFragment and display a message
     *
     *  @param name The name of the chat room that was searched for
     */
    fun showRoomFailedToLoadMessage(name: String)

    /**
     * Set up the dialog for a user to choose who they want to send tokens to
     *
     * @param names List of user names that the user has a direct message room open with
     */
    fun setupSendToDialog(names: List<String>)

    /**
     * Go to a Transaction Activity
     *
     * @param walletAddress the recipient's wallet address
     */
    fun directToTransaction(walletAddress: String)

    /**
     * Go to the direct message room of the recipient, and then
     *  immediately to a Transaction Activity
     *
     * @param name the recipient's Rocket.chat username
     */
    fun directToDmRoom(name: String)

    /**
     * Populate the recycler view of transaction history with transactions
     *
     * @param txs List of transaction models
     */
    fun updateTransactions(txs: List<TransactionViewModel>)

    fun showLoading()

    fun hideLoading()

}
