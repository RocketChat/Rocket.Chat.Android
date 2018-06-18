package chat.rocket.android.main.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import chat.rocket.android.R

class MainNavigationAdapter(ctx: Context) :
    WearableNavigationDrawerView.WearableNavigationDrawerAdapter() {

    private val context: Context = ctx

    override fun getItemText(pos: Int): CharSequence {
        when (pos) {
            0 -> return context.getString(R.string.title_chat_rooms)
            1 -> return context.getString(R.string.title_settings)
            2 -> return context.getString(R.string.title_logout)
        }
        return ""
    }

    override fun getItemDrawable(pos: Int): Drawable {
        when (pos) {
            0 -> return context.getDrawable(R.drawable.ic_chat_rooms_white_24dp)
            1 -> return context.getDrawable(R.drawable.ic_settings_white_24dp)
            2 -> return context.getDrawable(R.drawable.ic_logout_white_24dp)
        }
        return context.getDrawable(R.drawable.ic_chat_rooms_white_24dp)
    }

    override fun getCount(): Int {
        return 3;
    }
}