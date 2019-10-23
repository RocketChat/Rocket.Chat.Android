package chat.rocket.android.members.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.RecyclerViewItemCountAssertion.Companion.withItemCount
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.loginUserToTheApp
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.MEMBERS
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.TEST_CHANNEL


class MembersFragmentTest {

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
            navigateToChannelDetails()
        } catch (e: NoMatchingViewException) {
            navigateToChannelDetails()
        }
    }

    @Test
    fun members_should_be_greater_than_zero(){
        onView(withText(MEMBERS)).perform(click())
        Thread.sleep(6000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
    }

    private fun navigateToChannelDetails() {
        Thread.sleep(3000)
        onView(withText(TEST_CHANNEL)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }
}