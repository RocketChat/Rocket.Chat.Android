package chat.rocket.android.favoritemessages.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.RecyclerViewItemCountAssertion.Companion.withItemCount
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.FAVORITE_MESSAGES
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.USERNAME
import testConfig.Config.Companion.serverUrl


class FavoriteMessagesFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
        } catch (e: NoMatchingViewException) {
        }
    }

    @Test
    fun messages_should_be_greater_than_zero(){
        navigateToGeneralChannelDetails()
        onView(withText(FAVORITE_MESSAGES)).perform(click())
        Thread.sleep(6000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
    }

    @Test
    fun messages_should_be_zero(){
        navigateToSandboxChannelDetails()
        onView(withText(FAVORITE_MESSAGES)).perform(click())
        Thread.sleep(6000)
        onView(withId(R.id.recycler_view)).check(withItemCount(equalTo(0)))
    }


    private fun loginIfUserIsLoggedOut(){
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(serverUrl)
        }
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME),
            closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }

    private fun navigateToSandboxChannelDetails() {
        Thread.sleep(5000)
        onView(withText("sandbox")).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }

    private fun navigateToGeneralChannelDetails() {
        Thread.sleep(5000)
        onView(withText("general")).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }
}