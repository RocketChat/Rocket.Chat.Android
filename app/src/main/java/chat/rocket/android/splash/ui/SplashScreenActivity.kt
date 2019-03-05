package chat.rocket.android.splash.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.authentication.ui.AuthenticationActivity


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_splash_screen)

        Handler().postDelayed({
            val intent = Intent(this, AuthenticationActivity::class.java)
            //finishActivity()
            startActivity(intent)

        }, 2000)
    }

}