package chat.rocket.android.wallet.create.ui

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
import kotlinx.android.synthetic.main.app_bar_create_wallet.*
import javax.inject.Inject

class CreateWalletActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)

        setupToolbar()
        addFragment("CreateWalletFragment")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun addFragment(tag: String) {
        addFragment(tag, R.id.fragment_container) {
            CreateWalletFragment.newInstance()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        text_create_wallet.textContent = resources.getString(R.string.title_create_wallet)
    }

    fun setupResultAndFinish(walletName: String, password: String, mnemonic: String) {
        if (walletName.isEmpty() || password.isEmpty()) {
            setResult(Activity.RESULT_CANCELED)
        }
       else {
            var result = Intent()
            result.putExtra("walletName", walletName)
            result.putExtra("password", password)
            result.putExtra("mnemonic", mnemonic)
            setResult(Activity.RESULT_OK, result)
        }

        finish()
    }


}