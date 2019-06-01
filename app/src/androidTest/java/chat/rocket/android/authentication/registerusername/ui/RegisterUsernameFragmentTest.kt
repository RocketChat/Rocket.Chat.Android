package chat.rocket.android.authentication.registerusername.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.matchers.withHint
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class RegisterUsernameFragmentTest {

    private val USER_ID = "user_id"
    private val AUTH_TOKEN = "auth_token"
    private val USERNAME: String = "testuser"


    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.RegisterUsername.screenName, R.id.fragment_container) {
            newInstance(USER_ID, AUTH_TOKEN)
        }
    }


    @Test
    fun check_UI_elements(){
        onView(withId(R.id.text_sign_in_to_your_server)).check(matches(withText("Register username")))
        onView(withId(R.id.text_username)).check(matches(withHint("Username")))
        onView(withId(R.id.button_use_this_username)).check(matches(withText("Use this username")))
    }

    @Test
    fun click_register_username(){
        onView(withId(R.id.text_username)).perform(typeText(USERNAME), closeSoftKeyboard())
        onView(withId(R.id.button_use_this_username)).perform(click())
    }
}