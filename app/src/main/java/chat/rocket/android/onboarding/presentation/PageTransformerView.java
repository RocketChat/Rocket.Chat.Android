package chat.rocket.android.onboarding.presentation;

import android.view.View;

public interface PageTransformerView {

    /**
     * The page is being scrolled.
     */
    void onPageScrolled(View page, float position);
}
