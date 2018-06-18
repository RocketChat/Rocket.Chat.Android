package chat.rocket.android.chatroom.ui

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import dagger.android.*
import javax.inject.Inject

class ChatRoomActivity : Activity(), HasActivityInjector, HasFragmentInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    override fun fragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

}