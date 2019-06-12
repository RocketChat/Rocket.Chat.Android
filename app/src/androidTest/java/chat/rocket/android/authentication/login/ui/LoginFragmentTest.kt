package chat.rocket.android.authentication.login.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.UITestConfig.Companion.PASSWORD
import chat.rocket.android.UITestConfig.Companion.USERNAME
import chat.rocket.android.UITestConfig.Companion.serverUrl
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.matchers.withHint
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class LoginFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            newInstance(serverUrl)
        }
    }

    @Test
    fun check_UI_elements(){
        onView(withId(R.id.text_login)).check(matches(withText("Login")))
        onView(withId(R.id.text_username_or_email)).check(matches(withHint("Username or email")))
        onView(withId(R.id.text_password)).check(matches(withHint("Password")))
        onView(withId(R.id.button_log_in)).check(matches(withText("Login")))
        onView(withId(R.id.button_forgot_your_password)).check(matches(withText("Forgot your password?")))
    }

    @Test
    fun login_button_enable_if_details_are_filled(){
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(
            typeText(PASSWORD), closeSoftKeyboard()
        )
        onView(withId(R.id.button_log_in)).check(matches(isEnabled()))
    }

    @Test
    fun login_button_disable_if_details_are_not_filled(){
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.button_log_in)).check(matches(not(isEnabled())))
    }

    //@Test
    fun check_login_with_email_and_logout(){
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(
            typeText(PASSWORD), closeSoftKeyboard()
        )
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_logout)).check(matches(isDisplayed()))
            .perform(click())
        onView(withText("LOGOUT")).perform(click())
    }
}