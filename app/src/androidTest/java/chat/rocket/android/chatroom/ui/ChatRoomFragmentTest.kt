package chat.rocket.android.chatroom.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.withIndex
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.EXISTING_CHANNEL
import testConfig.Config.Companion.EXISTING_USER
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
    fun check_UI_elements_when_messages_are_send() {
        navigateToExistingChannel()
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_message_list)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_message_composer)).check(matches(isDisplayed()))
    }

    @Test
    fun check_UI_elements_when_no_messages_is_send() {
        navigateToExistingUser()
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
    fun emoji_keyboard_should_be_displayed() {
        navigateToExistingChannel()
        onView(withId(R.id.button_add_reaction_or_show_keyboard)).perform(click())
        onView(withId(R.id.emoji_keyboard_container)).check(matches(isDisplayed()))
        onView(withId(R.id.emoji_recycler_view)).check(matches(isDisplayed()))
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
        Thread.sleep(5000)
        onView(withText(SANDBOX)).perform(click())
        Thread.sleep(2000)
    }

    private fun navigateToExistingUser() {
        Thread.sleep(5000)
        onView(withText(EXISTING_USER)).perform(click())
        Thread.sleep(2000)
    }

//    @Test
//    fun showFileSelection_nonNullFiltersAreApplied() {
//        val fragment =
//            activityRule.activity.supportFragmentManager.findFragmentByTag("ChatRoomFragment") as ChatRoomFragment
//
//        val filters = arrayOf("image/*")
//        fragment.showFileSelection(filters)
//
//        intended(
//            allOf(
//                hasAction(Intent.ACTION_GET_CONTENT),
//                hasType("*/*"),
//                hasCategories(setOf(Intent.CATEGORY_OPENABLE)),
//                hasExtra(Intent.EXTRA_MIME_TYPES, filters)
//            )
//        )
//    }
//
//    @Test
//    fun showFileSelection_nullFiltersAreNotApplied() {
//        val fragment =
//            activityRule.activity.supportFragmentManager.findFragmentByTag("ChatRoomFragment") as ChatRoomFragment
//
//        fragment.showFileSelection(null)
//
//        intended(
//            allOf(
//                hasAction(Intent.ACTION_GET_CONTENT),
//                hasType("*/*"),
//                hasCategories(setOf(Intent.CATEGORY_OPENABLE)),
//                not(hasExtraWithKey(Intent.EXTRA_MIME_TYPES))
//            )
//        )
//    }
}