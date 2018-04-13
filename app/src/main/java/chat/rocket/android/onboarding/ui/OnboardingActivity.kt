package chat.rocket.android.onboarding.ui

import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import androidx.view.size
import chat.rocket.android.R
import chat.rocket.android.onboarding.adapter.OnboardingPagerAdapter
import chat.rocket.android.onboarding.presentation.PageTransformerPresenter
import chat.rocket.android.util.extensions.setVisible
import kotlinx.android.synthetic.main.activity_onboarding.*
import timber.log.Timber
import java.util.*

class OnboardingActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val adapter = OnboardingPagerAdapter(supportFragmentManager)
        view_pager.adapter = adapter
        pageIndicator.initViewPager(view_pager)

        button_next.setOnClickListener{
            if (view_pager.currentItem != adapter.count-1){
                view_pager.currentItem = view_pager.currentItem+1
            }else{
                finish()
            }
        }

        button_skip.setOnClickListener{
            finish()
        }

        view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                val topRightX = image_mobile.x + image_mobile.width
                val topRightY = image_mobile.y
                when(position){
                    0->{
                        if (positionOffset!=0f){
                            image_mobile.translationY = -(80.px)*positionOffset
                            image_mobile.scaleX = 1.0f+positionOffset/2
                            image_mobile.scaleY = 1.0f+positionOffset/2
                            view_circle.alpha = positionOffset*positionOffset
                            text_channel_symbol.alpha = positionOffset*positionOffset
                            view_circle.x = topRightX - view_circle.width/2
                            view_circle.y = topRightY - view_circle.height
                            text_channel_symbol.x = view_circle.x + view_circle.width/2 - text_channel_symbol.width/2
                            text_channel_symbol.y = view_circle.y + view_circle.height/2 - text_channel_symbol.height/2

                        }

                        button_skip.setVisible(true)
                        button_next.text = resources.getString(R.string.action_next)
                    }
                    1->{
                        image_chat.x = view_circle.x + view_circle.width/2 - image_chat.width/2
                        image_chat.y = view_circle.y + view_circle.height/2 - image_chat.height/2
                        if (positionOffset<0.5){
                            view_circle.scaleX = 1-positionOffset*2
                            view_circle.scaleY = 1-positionOffset*2
                            text_channel_symbol.scaleX = 1-positionOffset*2
                            text_channel_symbol.scaleY = 1-positionOffset*2
                            text_channel_symbol.setVisible(true)
                            image_chat.setVisible(false)
                        }else{
                            view_circle.scaleX = (positionOffset-0.5f)*2
                            view_circle.scaleY = (positionOffset-0.5f)*2
                            image_chat.scaleX = (positionOffset-0.5f)*2
                            image_chat.scaleY = (positionOffset-0.5f)*2
                            text_channel_symbol.setVisible(false)
                            image_chat.setVisible(true)
                        }

                        button_skip.setVisible(true)
                        button_next.text = resources.getString(R.string.action_next)
                    }
                    2 ->{
                        button_skip.setVisible(false)
                        button_next.text = resources.getString(R.string.action_done)
                    }
                }
            }

            override fun onPageSelected(position: Int) {}

        })

        view_pager.setPageTransformer(false,PageTransformerPresenter())

    }

    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onBackPressed() {
        if (view_pager.currentItem !=0){
            view_pager.currentItem = view_pager.currentItem - 1
        }else{
            finish()
        }
    }
}
