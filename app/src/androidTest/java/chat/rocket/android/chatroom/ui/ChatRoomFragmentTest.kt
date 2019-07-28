package chat.rocket.android.chatroom.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.ScrollToTop
import chat.rocket.android.matchers.clickChildViewWithId
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.EXISTING_USER
import testConfig.Config.Companion.EXISTING_USER2
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.SANDBOX
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.TEST_MESSAGE
import testConfig.Config.Companion.USERNAME

@LargeTest
class ChatRoomFragmentTest {

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
    fun check_UI_elements_when_messages_are_present() {
        navigateToExistingChannel()
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_message_list)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_message_composer)).check(matches(isDisplayed()))
    }

    @Test
    fun check_UI_elements_when_no_messages_is_present() {
        navigateToExistingUser1()
        onView(withId(R.id.layout_message_list)).check(matches(isDisplayed()))
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_message_composer)).check(matches(isDisplayed()))
        onView(withId(R.id.image_chat_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.text_chat_title)).check(matches(withText(R.string.msg_no_chat_title)))
        onView(withId(R.id.text_chat_description)).check(matches(withText(R.string.msg_no_chat_description)))
    }

    @Test
    fun check_UI_elements_of_message_composer() {
        navigateToExistingChannel()
        onView(withId(R.id.button_add_reaction_or_show_keyboard)).check(matches(isDisplayed()))
        onView(withId(R.id.text_message)).check(matches(isDisplayed()))
        onView(withId(R.id.button_show_attachment_options)).check(matches(isDisplayed()))
    }

    @Test
    fun check_UI_elements_of_message_item() {
        navigateToExistingUser2()
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
        onView(withId(R.id.image_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.text_sender)).check(matches(isDisplayed()))
        onView(withId(R.id.text_message_time)).check(matches(isDisplayed()))
        onView(withId(R.id.text_content)).check(matches(isDisplayed()))
        onView(withId(R.id.text_content)).check(matches(isDisplayed()))
    }

    @Test
    fun show_attachment_options() {
        navigateToExistingChannel()
        onView(withId(R.id.button_show_attachment_options)).perform(click())
        onView(withId(R.id.button_take_a_photo)).check(matches(isDisplayed()))
        onView(withId(R.id.button_attach_a_file)).check(matches(isDisplayed()))
        onView(withId(R.id.button_drawing)).check(matches(isDisplayed()))
        onView(withId(R.id.text_message)).check(matches(isDisplayed()))
    }

    @Test
    fun send_text_message() {
        navigateToExistingChannel()
        onView(withId(R.id.text_message)).check(matches(withHint(R.string.msg_message)))
            .perform(
            typeText(TEST_MESSAGE), closeSoftKeyboard()
        )
        onView(withId(R.id.button_send)).check(matches(isDisplayed()))
            .perform(click())
        onView(withId(R.id.text_message)).perform(clearText())
    }

    @Test
    fun check_message_action_bottom_sheet() {
        navigateToExistingUser2()
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withText(R.string.action_msg_add_reaction)).check(matches(isDisplayed()))
        onView(withText(R.string.action_msg_reply)).check(matches(isDisplayed()))
        onView(withText(R.string.action_msg_quote)).check(matches(isDisplayed()))
        onView(withText(R.string.action_msg_permalink)).check(matches(isDisplayed()))
        onView(withText(R.string.action_msg_copy)).check(matches(isDisplayed()))
        onView(withText(R.string.action_msg_edit)).check(matches(isDisplayed()))
        onView(withText(R.string.action_info)).check(matches(isDisplayed()))
    }

    @Test
    fun clicking_user_avatar_should_open_his_details(){
        navigateToExistingUser2()
        onView(withId(R.id.recycler_view)).perform(ScrollToTop())
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, clickChildViewWithId(R.id.image_avatar)
            )
        )
        onView(withId(R.id.user_details_layout)).check(matches(isDisplayed()))
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
        }
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME),
            closeSoftKeyboard()
        )
        onView(withId(R.id.text_password))
            .perform(typeText(PASSWORD),closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }

    private fun navigateToExistingChannel() {
        onView(withText(SANDBOX)).perform(click())
        Thread.sleep(2000)
    }

    private fun navigateToExistingUser1() {
        onView(withText(EXISTING_USER)).perform(click())
        Thread.sleep(2000)
    }

    private fun navigateToExistingUser2() {
        onView(withText(EXISTING_USER2)).perform(click())
        Thread.sleep(2000)
    }
}