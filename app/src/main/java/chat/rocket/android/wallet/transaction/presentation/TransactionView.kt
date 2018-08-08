package chat.rocket.android.wallet.transaction.presentation

import java.math.BigDecimal

interface TransactionView {

    /**
     * Update the sender's address and display the sender/user's balance
     *
     * @param address sender's address
     * @param balance sender's current wallet balance
     */
    fun showUserWallet(address: String, balance: BigDecimal)

    /**
     * Display the recipient's wallet address
     *
     * @param address recipient's wallet address
     */
    fun showRecipientAddress(address: String)

    /**
     * Finish up the activity and show successful transaction message
     *
     * @param amount the amount of tokens in the transaction
     * @param txHash the hash of the transaction
     */
    fun showSuccessfulTransaction(amount: Double, txHash: String, reason: String)

    /**
     * Show error for the recipient not having a wallet address
     */
    fun showNoAddressError()

    /**
     * Show error for the current user not having a wallet
     */
    fun showNoWalletError()

    /**
     * Show message that the transaction failed, which can be a more detailed message
     *
     * @param msg More detailed message of why the transaction failed
     */
    fun showTransactionFailedMessage(msg: String?)

    fun showLoading()

    fun hideLoading()
}