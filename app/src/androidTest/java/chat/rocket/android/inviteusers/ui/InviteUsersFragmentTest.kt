package chat.rocket.android.inviteusers.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.RecyclerViewItemCountAssertion
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.MEMBERS
import testConfig.Config.Companion.NON_EXISTING_USER
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.TEST_CHANNEL3
import testConfig.Config.Companion.TEST_USER2
import testConfig.Config.Companion.USERNAME


class InviteUsersFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
            navigateToInviteUser()
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
            navigateToInviteUser()
        }
    }

    private fun navigateToInviteUser() {
        onView(withText(TEST_CHANNEL3)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
        onView(withText(MEMBERS)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.button_invite_user)).perform(click())
    }

    @Test
    fun check_UI_element() {
        onView(withId(R.id.text_invite_users)).check(matches(withHint(R.string.msg_invite_members)))
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
        onView(withId(R.id.button_invite_user)).check(matches(isDisplayed()))
    }

    @Test
    fun search_an_existing_user() {
        onView(withId(R.id.text_invite_users)).perform(
            typeText(TEST_USER2), closeSoftKeyboard()
        )
        Thread.sleep(2000)
        onView(withId(R.id.recycler_view)).check(
            RecyclerViewItemCountAssertion.withItemCount(greaterThan(0))
        )
        onView(withId(R.id.text_member)).check(matches(withText(TEST_USER2)))
    }

    @Test
    fun search_an_non_existing_user() {
        onView(withId(R.id.text_invite_users)).perform(
            typeText(NON_EXISTING_USER), closeSoftKeyboard()
        )
        Thread.sleep(2000)
        onView(withId(R.id.recycler_view)).check(
            RecyclerViewItemCountAssertion.withItemCount(equalTo(0))
        )
        onView(withId(R.id.text_member_not_found)).check(matches(isDisplayed()))
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
        }
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }
}