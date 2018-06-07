package chat.rocket.android.main.ui

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.wear.widget.drawer.WearableNavigationDrawerView
import android.support.wearable.activity.WearableActivity
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.main.presentation.MainView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class MainActivity : WearableActivity(), MainView, HasActivityInjector, HasSupportFragmentInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: MainPresenter
    private lateinit var navigationDrawer: WearableNavigationDrawerView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setAmbientEnabled()
        setUpTopNavigationDrawer()
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    private fun setUpTopNavigationDrawer() {
        navigationDrawer = findViewById(R.id.top_navigation_drawer)
        val mainNavigationAdapter = MainNavigationAdapter(this)
        navigationDrawer.setAdapter(mainNavigationAdapter)
        navigationDrawer.controller.peekDrawer()
        navigationDrawer.addOnItemSelectedListener { pos ->
            Toast.makeText(this, "Selected position $pos", Toast.LENGTH_SHORT).show()
            //Add various fragments here
//            when(pos){
//                1->{
//
//                }
//            }
        }
    }

}