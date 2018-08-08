package chat.rocket.android.wallet.transaction.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.textContent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject
import kotlinx.android.synthetic.main.app_bar_transaction.toolbar
import kotlinx.android.synthetic.main.app_bar_transaction.text_transaction

class TransactionActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        setupToolbar()
        addFragment("TransactionFragment")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setupResultAndFinish("", 0.0,"", "")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    fun setupResultAndFinish(recipient: String, amount: Double, txHash: String, reason: String) {
        if (recipient.isEmpty() || amount <= 0.0) {
            setResult(Activity.RESULT_CANCELED)
        }
        else {
            val result = Intent()
            result.putExtra("recipientId", recipient)
            result.putExtra("amount", amount)
            result.putExtra("transaction_hash", txHash)
            result.putExtra("reason", reason)
            setResult(Activity.RESULT_OK, result)
        }
        finish()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun addFragment(tag: String) {
        addFragment(tag, R.id.fragment_container) {
            TransactionFragment.newInstance()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        text_transaction.textContent = resources.getString(R.string.title_transaction)
    }
}