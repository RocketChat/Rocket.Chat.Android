package chat.rocket.android.chatdetails.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.withTextInChild
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.EXISTING_CHANNEL
import testConfig.Config.Companion.EXISTING_USER
import testConfig.Config.Companion.FAVORITE_MESSAGES
import testConfig.Config.Companion.FILES
import testConfig.Config.Companion.MEMBERS
import testConfig.Config.Companion.MENTIONS
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.PINNED_MESSAGES
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.USERNAME

class ChatDetailsFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
        }
    }

    @Test
    fun check_UI_elements() {
        navigateToExistingChannelDetails()
        onView(withId(R.id.title_description)).check(matches(withText(R.string.title_description)))
        onView(withId(R.id.content_description)).check(matches(isDisplayed()))
        onView(withId(R.id.title_topic)).check(matches(withText(R.string.title_topic)))
        onView(withId(R.id.content_topic)).check(matches(isDisplayed()))
        onView(withId(R.id.title_announcement)).check(matches(withText(R.string.title_announcement)))
        onView(withId(R.id.content_announcement)).check(matches(isDisplayed()))
    }

    @Test
    fun check_UI_elements_of_option_list_in_channel() {
        navigateToExistingChannelDetails()
        onView(withId(R.id.options)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, withTextInChild(R.id.name, FILES))
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, withTextInChild(R.id.name, MENTIONS))
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, withTextInChild(R.id.name, MEMBERS))
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(3, withTextInChild(R.id.name, FAVORITE_MESSAGES))
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(4, withTextInChild(R.id.name, PINNED_MESSAGES))
        )
    }

    @Test
    fun check_UI_elements_of_option_list_in_direct_messages() {
        navigateToExistingDMDetails()
        onView(withId(R.id.options)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, withTextInChild(R.id.name, FILES))
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, withTextInChild(R.id.name, FAVORITE_MESSAGES))
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, withTextInChild(R.id.name, PINNED_MESSAGES))
        )
    }

    @Test
    fun files_fragment_should_open() {
        navigateToExistingChannelDetails()
        onView(withText(FILES)).perform(click())
        onView(withId(R.id.files_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun members_fragment_should_open() {
        navigateToExistingChannelDetails()
        onView(withText(MEMBERS)).perform(click())
        onView(withId(R.id.members_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun mentions_fragment_should_open() {
        navigateToExistingChannelDetails()
        onView(withText(MENTIONS)).perform(click())
        onView(withId(R.id.mentions_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun favourite_messages_fragment_should_open() {
        navigateToExistingChannelDetails()
        onView(withText(FAVORITE_MESSAGES)).perform(click())
        onView(withId(R.id.favourite_message_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun pinned_messages_fragment_should_open() {
        navigateToExistingChannelDetails()
        onView(withText(PINNED_MESSAGES)).perform(click())
        onView(withId(R.id.pinned_messages_layout)).check(matches(isDisplayed()))
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
        }
        onView(withId(R.id.text_username_or_email)).perform(typeText(USERNAME), closeSoftKeyboard())
        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }

    private fun navigateToExistingDMDetails() {
        Thread.sleep(5000)
        onView(withText(EXISTING_USER)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }

    private fun navigateToExistingChannelDetails() {
        Thread.sleep(5000)
        onView(withText(EXISTING_CHANNEL)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }
}