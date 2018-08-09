package chat.rocket.android.wallet.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extensions.*
import chat.rocket.android.wallet.create.ui.CreateWalletActivity
import chat.rocket.android.wallet.presentation.WalletPresenter
import chat.rocket.android.wallet.presentation.WalletView
import chat.rocket.android.wallet.transaction.ui.TransactionActivity
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.mnemonic_dialog.view.*
import kotlinx.android.synthetic.main.wallet_send_to_dialog.view.*
import javax.inject.Inject

class WalletFragment : Fragment(), WalletView {
    @Inject lateinit var presenter: WalletPresenter

    private val NEW_WALLET_REQUEST = 1
    private val RESULT_OK = -1


    companion object {
        fun newInstance() = WalletFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        // Set up transaction list (recycler view)
        setupRecyclerView(view)

        // Check if user has a wallet
        presenter.loadWallet(this.activity as MainActivity)
    }

    private fun setupToolbar() {
        (activity as MainActivity).toolbar.title = getString(R.string.title_wallet)
    }

    private fun setupRecyclerView(view: View) {
        val transactionsRecyclerView = view.findViewById<RecyclerView>(R.id.transactions_recyclerView)
        val linearLayoutManager = LinearLayoutManager(this.activity)
        transactionsRecyclerView.layoutManager = linearLayoutManager
        val adapter = WalletAdapter()
        transactionsRecyclerView.adapter = adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_wallet)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Load room names for the send-to dialog
        presenter.loadDMRooms()
        button_create_wallet.setOnClickListener {
            ui {
                val intent = Intent(activity, CreateWalletActivity::class.java)
                intent.putExtra("user_name", presenter.getUserName())
                startActivityForResult(intent, NEW_WALLET_REQUEST)
                activity?.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update the wallet (currently mainly the balance)
        presenter.loadWallet(this.activity as MainActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == NEW_WALLET_REQUEST){
            if(resultCode == RESULT_OK){

                val mnemonicDialogView = LayoutInflater.from(activity).inflate(R.layout.mnemonic_dialog, null)
                val mnemonicDialogBuilder = AlertDialog.Builder(activity)
                        .setView(mnemonicDialogView)

                // display phrase
                var mnemonic = data!!.getStringExtra("mnemonic")
                mnemonicDialogView.mnemonic.textContent = mnemonic

                // show dialog
                val mnemonicAlertDialog = mnemonicDialogBuilder.create()

                // set listener
                mnemonicDialogView.button_mnemonic_saved.setOnClickListener{
                    mnemonicAlertDialog.dismiss()
                }

                mnemonicAlertDialog.show()
            }
        }
    }

    override fun updateTransactions(txs: List<TransactionViewModel>) {
        ui {
            val adapter = (transactions_recyclerView.adapter as WalletAdapter)

            adapter.updateTransactions(txs)

            adapter.notifyDataSetChanged()
        }
    }

    override fun setupSendToDialog(names: List<String>) {
        button_sendToken?.setOnClickListener {
            val dialogLayout = layoutInflater.inflate(R.layout.wallet_send_to_dialog, null)
            val adapter: ArrayAdapter<String> = ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, names)
            val textView: AutoCompleteTextView = dialogLayout.findViewById(R.id.search_users_autoCompleteTextView)
            textView.setAdapter(adapter)
            textView.onFocusChangeListener = View.OnFocusChangeListener{
                view, b ->
                if (b) {
                    textView.showDropDown()
                }
            }
            // Update the wallet address textEdit text when a username is chosen
            textView.setOnItemClickListener { parent, view, position, id ->
                val recipient = dialogLayout.search_users_autoCompleteTextView.textContent
                presenter.loadWalletAddress(recipient) {
                    dialogLayout.recipient_address_editText.textContent = it
                }
            }
            // Setup radio buttons
            dialogLayout.radio_group_send_to.setOnCheckedChangeListener { group, checkedId ->
                when {
                    dialogLayout.search_users_radioButton.isChecked -> {
                        dialogLayout.search_users_autoCompleteTextView.requestFocus()
                        dialogLayout.recipient_address_editText.isEnabled = false
                    }
                    dialogLayout.qr_radioButton.isChecked -> {
                        dialogLayout.recipient_address_editText.isEnabled = false
                    }
                    else -> {
                        dialogLayout.recipient_address_editText.isEnabled = true
                        dialogLayout.recipient_address_editText.requestFocus()
                    }
                }
            }

            val dialogSendTo = AlertDialog.Builder(context)
                    .setTitle("Find Recipient")
                    .setView(dialogLayout)
                    .setNegativeButton("Cancel", { dialog, _ -> dialog.dismiss() })
                    .setPositiveButton("Send", { dialog, _ ->
                        // Check that there is a valid wallet address in the editText
                        val address = dialogLayout.recipient_address_editText.textContent
                        if (address.length == 40 ||
                                (address.length == 42 && address.startsWith("0x"))) {
                            if (dialogLayout.search_users_radioButton.isChecked) {
                                directToDmRoom(dialogLayout.search_users_autoCompleteTextView.textContent)
                            } else {
                                directToTransaction(dialogLayout.recipient_address_editText.textContent)
                            }
                            dialog.dismiss()
                        } else {
                            showToast("No wallet address found or invalid address format", Toast.LENGTH_LONG)
                        }
                    })

            dialogSendTo.show()
        }
    }

    override fun directToTransaction(walletAddress: String) {
        ui {
            val intent = Intent(activity, TransactionActivity::class.java)
            intent.putExtra("recipient_address", walletAddress)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
        }
    }

    override fun directToDmRoom(name: String) {
        presenter.loadDMRoomByName(name)
    }

    override fun showRoomFailedToLoadMessage(name: String) {
        showToast("No direct message chat room open with user: $name", Toast.LENGTH_LONG)
    }

    override fun showBalance(bal: Double) {
        // TODO eventually allow user to have/access multiple wallets
        textView_balance.textContent = bal.toString()
    }

    override fun showWallet(value: Boolean, bal: Double) {
        button_create_wallet.setVisible(!value)
        button_sendToken.setVisible(value)
        textView_transactions.setVisible(value)
        textView_balance.setVisible(value)
        textView_wallet_title.setVisible(value)
        divider_wallet.setVisible(value)
        if (value)
            showBalance(bal)
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
        enableUserInput(true)
    }

    private fun enableUserInput(value: Boolean) {
        ui {
            button_create_wallet.isEnabled = value
            button_sendToken.isEnabled = value
        }
    }
}
