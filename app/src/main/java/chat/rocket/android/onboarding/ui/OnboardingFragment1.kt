package chat.rocket.android.onboarding.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.onboarding.presentation.PageTransformerView
import kotlinx.android.synthetic.main.fragment_onboarding_fragment.*
import kotlinx.android.synthetic.main.fragment_onboarding_fragment.view.*

class OnboardingFragment1 : Fragment(), PageTransformerView {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_onboarding_fragment, container, false)
        view.text_message.text = resources.getString(R.string.onBoarding_title1)
        view.text_description.text = resources.getString(R.string.onBoarding_description1)
        view.tag = this
        return view
    }

    override fun onPageScrolled(page: View?, position: Float) {
        val pageWidth = page?.width
        val pageWidthTimesPosition = pageWidth!! * position
        val absPosition = Math.abs(position)

        text_message.alpha = 1.0f - absPosition*1.5f
        text_message.translationX = pageWidthTimesPosition * 0.1f

        text_description.alpha = 1.0f - absPosition*1.5f
        text_description.translationX = pageWidthTimesPosition * 0.3f

        image_app_name.alpha = 1.0f - absPosition*2
        image_app_icon.alpha = 1.0f - absPosition*2

        
    }
}
