package chat.rocket.android.authentication.twofactor.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.UITestConfig.Companion.CODE
import chat.rocket.android.UITestConfig.Companion.PASSWORD
import chat.rocket.android.UITestConfig.Companion.USERNAME
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class TwoFAFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.TwoFa.screenName, R.id.fragment_container) {
            newInstance(USERNAME, PASSWORD)
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_two_factor_authentication)).check(matches(ViewMatchers.withText("Two-factor Authentication")))
        onView(withId(R.id.text_two_factor_authentication_code)).check(matches(isDisplayed()))
        onView(withId(R.id.button_confirm)).check(matches(ViewMatchers.withText("Confirm")))
    }

    @Test
    fun fill_code_and_click_confirm() {
        onView(withId(R.id.text_two_factor_authentication_code)).perform(
            typeText(CODE), closeSoftKeyboard()
        )
        onView(withId(R.id.button_confirm)).perform(click())
    }
}