package chat.rocket.android.util

import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Root
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import chat.rocket.android.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.TypeSafeMatcher
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.USERNAME


fun withHint(expectedHint: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        override fun matchesSafely(view: View): Boolean {
            if (view !is EditText) {
                return false
            }

            val hint = view.hint.toString()
            return expectedHint == hint
        }

        override fun describeTo(description: Description) {}
    }
}

fun withToolbarTitle(textMatcher: Matcher<CharSequence>): Matcher<Any> {
    return object : BoundedMatcher<Any, Toolbar>(Toolbar::class.java) {
        public override fun matchesSafely(toolbar: Toolbar): Boolean {
            return textMatcher.matches(toolbar.title)
        }

        override fun describeTo(description: Description) {
            description.appendText("with toolbar title: ")
            textMatcher.describeTo(description)
        }
    }
}

fun clickChildViewWithId(id: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return null
        }

        override fun getDescription(): String {
            return ""
        }

        override fun perform(uiController: UiController, view: View) {
            val v = view.findViewById<View>(id)
            v.performClick()
        }
    }
}

//Matches an element with id as a childView
fun withTextInChild(id: Int, text: String): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return allOf(
                isAssignableFrom(EditText::class.java),
                isAssignableFrom(TextView::class.java)
            )
        }

        override fun getDescription(): String {
            return ""
        }

        override fun perform(uiController: UiController, view: View) {
            val v = view.findViewById<TextView>(id)
            v?.text = text
        }
    }
}

//Matches an element with an id at a particular index
fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}

class ScrollToBottom : ViewAction {
    override fun getDescription(): String {
        return "scroll RecyclerView to bottom"
    }

    override fun getConstraints(): Matcher<View> {
        return allOf<View>(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val itemCount = recyclerView.adapter?.itemCount
        val position = itemCount?.minus(1) ?: 0
        recyclerView.scrollToPosition(position)
        uiController?.loopMainThreadUntilIdle()
    }
}

class ScrollToTop : ViewAction {
    override fun getDescription(): String {
        return "scroll RecyclerView to bottom"
    }

    override fun getConstraints(): Matcher<View> {
        return allOf<View>(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        recyclerView.scrollToPosition(0)
        uiController?.loopMainThreadUntilIdle()
    }
}

fun loginUserToTheApp() {
    onView(ViewMatchers.withId(R.id.text_username_or_email)).perform(
        ViewActions.typeText(USERNAME),
        closeSoftKeyboard()
    )
    onView(ViewMatchers.withId(R.id.text_password))
        .perform(ViewActions.typeText(PASSWORD), closeSoftKeyboard())
    onView(ViewMatchers.withId(R.id.button_log_in)).perform(click())
    Thread.sleep(12000)
}

class RecyclerViewItemCountAssertion private constructor(private val matcher: Matcher<Int>) :
    ViewAssertion {

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        MatcherAssert.assertThat(adapter!!.itemCount, matcher)
    }

    companion object {

        fun withItemCount(matcher: Matcher<Int>): RecyclerViewItemCountAssertion {
            return RecyclerViewItemCountAssertion(matcher)
        }
    }
}

class ToastMatcher : TypeSafeMatcher<Root>() {

    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    public override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            if (windowToken === appToken) {
                // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return true
            }
        }
        return false
    }

    companion object {
        fun isToast(): Matcher<Root> {
            return ToastMatcher()
        }
    }
}