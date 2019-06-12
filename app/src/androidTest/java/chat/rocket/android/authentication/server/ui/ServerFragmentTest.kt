package chat.rocket.android.authentication.server.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.UITestConfig.Companion.SERVER
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class ServerFragmentTest{

    @JvmField
    var activityRule = ActivityTestRule<AuthenticationActivity>(AuthenticationActivity::class.java)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Server.screenName, R.id.fragment_container) {
            newInstance()
        }
    }

    @Test
    fun check_UI_elements(){
        onView(withId(R.id.image_server)).check(matches(isDisplayed()))
        onView(withId(R.id.spinner_server_protocol)).check(matches(isDisplayed()))
        onView(withId(R.id.server_url_container)).check(matches(isDisplayed()))
        onView(withId(R.id.button_connect)).check(matches(isDisplayed()))
        onView(withId(R.id.text_sign_in_to_your_server)).check(matches(withText("Sign in to your server")))
        onView(withId(R.id.text_server_url)).check(matches(withHint("your-company.rocket.chat")))
    }

    @Test
    fun fill_server_url_and_connect() {
        onView(withId(R.id.text_server_url)).perform(
            typeText(SERVER), closeSoftKeyboard()
        )
        onView(withId(R.id.button_connect)).perform(click())
    }
}