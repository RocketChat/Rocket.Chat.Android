package chat.rocket.android.servers.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.RecyclerViewItemCountAssertion
import chat.rocket.android.util.loginUserToTheApp
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.ORG_NAME
import testConfig.Config.Companion.SERVER_URL

class ServersBottomSheetFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
                chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
            }
            loginUserToTheApp()
            onView(withText(ORG_NAME)).perform(click())
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
            onView(withText(ORG_NAME)).perform(click())
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_server)).check(matches(withText("Server")))
        onView(withId(R.id.view_divider)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun no_of_available_server_should_be_greater_than_zero() {
        onView(withId(R.id.recycler_view)).check(
            RecyclerViewItemCountAssertion.withItemCount(
                Matchers.greaterThan(0)
            )
        )
    }
}