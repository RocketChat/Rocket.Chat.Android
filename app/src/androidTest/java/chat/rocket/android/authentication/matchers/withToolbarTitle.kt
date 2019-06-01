package chat.rocket.android.authentication.matchers

import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

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