package chat.rocket.android.onboarding.presentation

import android.support.v4.view.ViewPager
import android.view.View

class PageTransformerPresenter : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        if (page.tag is PageTransformerView) {
            val delegate = page.tag as PageTransformerView

            if (position == 0.0f) {
                // Page is selected
            } else if (position <= -1.0f || position >= 1.0f) {
                // Page not visible to the user
            } else {
                // Page is being scrolled
                delegate.onPageScrolled(page, position)
            }
        }
    }
}