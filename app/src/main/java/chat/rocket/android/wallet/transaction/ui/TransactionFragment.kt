package chat.rocket.android.wallet.transaction.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.*
import chat.rocket.android.wallet.transaction.presentation.TransactionView
import dagger.android.support.AndroidSupportInjection
import chat.rocket.android.wallet.transaction.presentation.TransactionPresenter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.coroutines.experimental.async
import java.math.BigDecimal


class TransactionFragment: Fragment(), TransactionView, ActionMode.Callback {
    @Inject lateinit var presenter: TransactionPresenter
    private var actionMode: ActionMode? = null
    private var recipientUserName: String = ""
    private var recipientAddress: String = ""
    private var senderAddress: String = ""
    private val disposables = CompositeDisposable()


    companion object {
        fun newInstance() = TransactionFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_transaction)

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)

        // get recipient username
        val nullableRecipientUserName = activity?.intent?.getStringExtra("recipient_user_name")
        recipientUserName = nullableRecipientUserName ?: ""
        recipient_textView.text = recipient_textView.text.toString().plus(recipientUserName)

        // get recipient wallet address
        recipientAddress = activity?.intent?.getStringExtra("recipient_address") ?: ""
        if (recipientAddress == "") {
            if (recipientUserName != "") {
                presenter.loadWalletAddress(recipientUserName) {
                    recipientAddress = it
                    if (recipientAddress == "") {
                        showNoAddressError()
                    } else {
                        showRecipientAddress(recipientAddress)
                    }
                }
            } else {
                showNoAddressError()
            }
        } else {
            showRecipientAddress(recipientAddress)
            if (recipientUserName.isEmpty()) {
                reasonLayout.isVisible = false
            }
        }

        // get sender's address and balance
        presenter.loadUserTokens()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposables.add(listenToChanges())
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    override fun showUserWallet(address: String, balance: BigDecimal) {
        senderAddress = address
        current_balance_textView.textContent = "Your Balance: " + balance.toString()
        enableUserInput(true)
    }

    override fun showRecipientAddress(address: String) {
        recipient_address_textView.textContent = address
    }

    override fun showNoAddressError() {
        showToast("Error: No recipient wallet address found!")
    }

    override fun showNoWalletError() {
        showToast("Error: You don't have a wallet!")
    }

    override fun showTransactionFailedMessage(msg: String?) {
        showToast(msg ?: "Transaction failed!")
    }

    override fun showSuccessfulTransaction(amount: Double, txHash: String, reason: String) {
        (activity as TransactionActivity).setupResultAndFinish(recipientUserName, amount, txHash, reason)
        showToast("Transaction Successful!")
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_password -> {
                // Send the transaction
                val amount = amount_tokens.text.toString().toDouble()
                val password = wallet_password_editText.textContent
                val reason = reason_editText.text.toString()
                val act = this.activity as TransactionActivity
                async{ presenter.sendTransaction(password, senderAddress, recipientAddress, amount, act, reason) }
                mode.finish()
                return true
            }
            else -> {
                false
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        mode.menuInflater.inflate(R.menu.password, menu)
        mode.title = getString(R.string.action_confirm_transaction)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
    }

    private fun listenToChanges(): Disposable {
        return Observables.combineLatest(amount_tokens.asObservable(),
                wallet_password_editText.asObservable()).subscribe {
            val amountText = amount_tokens.textContent

            if (recipientAddress.isNotEmpty() &&
                    wallet_password_editText.textContent.isNotEmpty() &&
                    (amountText.isNotEmpty() && amountText != "." && amountText.toDouble() > 0.0))
                startActionMode()
            else
                finishActionMode()
        }
    }

    private fun startActionMode() {
        if (actionMode == null) {
            actionMode = (activity as TransactionActivity).startSupportActionMode(this)
        }
    }

    private fun finishActionMode() = actionMode?.finish()

    private fun showToast(msg: String?) {
        ui {
            Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun showLoading() {
        enableUserInput(false)
        ui {
            view_loading.setVisible(true)
        }
    }

    override fun hideLoading() {
        ui {
            if (view_loading != null) {
                view_loading.setVisible(false)
            }
        }
    }

    private fun enableUserInput(value: Boolean) {
        ui {
            amountLayout.isEnabled = value
            reasonLayout.isEnabled = value
            wallet_password_editText.isEnabled = value
        }
    }

}