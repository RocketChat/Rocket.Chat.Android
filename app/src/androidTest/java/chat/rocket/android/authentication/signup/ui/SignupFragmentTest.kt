package chat.rocket.android.authentication.signup.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.UITestConfig.Companion.EMAIL
import chat.rocket.android.UITestConfig.Companion.NAME
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
class SignupFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.SignUp.screenName, R.id.fragment_container) {
            newInstance()
        }
    }

    @Test
    fun check_UI_elements(){
        onView(withId(R.id.text_sign_up)).check(matches(ViewMatchers.withText("Sign up")))
        onView(withId(R.id.text_name)).check(matches(ViewMatchers.withHint("Name")))
        onView(withId(R.id.text_username)).check(matches(ViewMatchers.withHint("Username")))
        onView(withId(R.id.text_password)).check(matches(ViewMatchers.withHint("Password")))
        onView(withId(R.id.text_email)).check(matches(ViewMatchers.withHint("Email")))
        onView(withId(R.id.button_register)).check(matches(ViewMatchers.withText("Register")))
    }

    @Test
    fun fill_details_and_signup() {
        onView(withId(R.id.text_name)).perform(
            typeText(NAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_username)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(
            typeText(PASSWORD), closeSoftKeyboard()
        )
        onView(withId(R.id.text_email)).perform(
            typeText(EMAIL), closeSoftKeyboard()
        )
        onView(withId(R.id.button_register)).perform(click())
    }
}