package chat.rocket.android.onboarding.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import chat.rocket.android.onboarding.ui.OnboardingFragment1
import chat.rocket.android.onboarding.ui.OnboardingFragment2
import chat.rocket.android.onboarding.ui.OnboardingFragment3


class OnboardingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {

        when (position) {
            0 -> return OnboardingFragment1()
            1 -> return OnboardingFragment2()
            2 -> return OnboardingFragment3()
            else -> return null
        }
    }

    override fun getCount(): Int {
        return 3
    }
}
