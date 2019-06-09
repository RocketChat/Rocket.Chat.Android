package chat.rocket.android.authentication.resetpassword.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.Config.Companion.EMAIL
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.matchers.withHint
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class ResetPasswordFragmentTest {


    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.ResetPassword.screenName, R.id.fragment_container) {
            newInstance()
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_reset_password)).check(matches(withText("Reset password")))
        onView(withId(R.id.text_email)).check(matches(withHint("Email")))
    }

    @Test
    fun check_reset_password() {
        onView(withId(R.id.text_email)).perform(
            typeText(EMAIL), closeSoftKeyboard()
        )
        onView(withId(R.id.button_reset_password)).perform(click())
        Thread.sleep(5000)
        onView(withId(R.id.image_on_boarding)).check(matches(isDisplayed()))
    }
}