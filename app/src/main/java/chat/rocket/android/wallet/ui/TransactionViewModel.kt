package chat.rocket.android.wallet.ui

import java.math.BigDecimal
import java.util.Date

class TransactionViewModel(hash: String, amount: BigDecimal, private val time: Long, sentFromUser: Boolean) {
    val txHash: String = hash
    val value: String = amount.toString()
    val timestamp: String
    // Whether this transaction was received from (true) or sent to (false) the current user
    val outgoingTx: Boolean = sentFromUser

    init {
        timestamp = generateTimestamp()
    }

    private fun generateTimestamp(): String {
        return if (time.compareTo(0) == 0) "Pending..." else Date(time*1000).toString()
    }
}