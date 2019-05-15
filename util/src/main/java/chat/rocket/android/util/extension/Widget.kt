package chat.rocket.android.util.extension

import android.content.Context
import android.os.Build
import android.widget.TextView
import androidx.appcompat.widget.SearchView

fun SearchView.onQueryTextListener(queryListener: (String) -> Unit) {
    return this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            queryListener(query)
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            queryListener(newText)
            return true
        }
    })
}

fun TextView.setTextViewAppearance(context: Context, style: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(style)
    } else {
        setTextAppearance(context, style)
    }
}