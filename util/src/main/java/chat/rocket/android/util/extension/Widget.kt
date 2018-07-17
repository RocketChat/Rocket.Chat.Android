package chat.rocket.android.util.extension

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