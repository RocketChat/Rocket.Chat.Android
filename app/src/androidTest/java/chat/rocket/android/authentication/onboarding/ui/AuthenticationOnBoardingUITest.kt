package chat.rocket.android.authentication.onboarding.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.authentication.ui.AuthenticationActivity
import org.junit.Rule
import org.junit.Test

@LargeTest
class AuthenticationOnBoardingUITest {

    @Rule
    @JvmField
    var activityRule = ActivityTestRule<AuthenticationActivity>(AuthenticationActivity::class.java)

    @Test
    fun check_UI_elements(){
        onView(withId(R.id.image_on_boarding)).check(matches(isDisplayed()))
        onView(withId(R.id.text_on_boarding_title)).check(matches(isDisplayed()))
        onView(withId(R.id.text_on_boarding_description)).check(matches(isDisplayed()))
        onView(withId(R.id.text_connect_with_server)).check(matches(withText("Connect with a server")))
        onView(withId(R.id.text_join_community)).check(matches(withText("Join in the community")))
        onView(withId(R.id.text_create_a_new_server)).check(matches(withText("Create a new server")))
    }

    @Test
    fun check_connect_with_server() {
        onView(withId(R.id.connect_with_a_server_container)).perform(click())
        onView(withId(R.id.text_server_url)).perform(
            typeText("open.rocket.chat"), closeSoftKeyboard()
        )
        onView(withId(R.id.button_connect)).perform(click())
    }

    @Test
    fun check_join_in_the_community_click() {
        onView(withId(R.id.join_community_container)).check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun check_create_new_server_click() {
        onView(withId(R.id.create_server_container)).check(matches(isDisplayed()))
            .perform(click())
    }
}