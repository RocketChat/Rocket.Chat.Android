package chat.rocket.android.thememanager.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.item_theme_row.view.*


class ThemesAdapter(private val themes: List<Theme>, private val listener: (Theme) -> Unit) : RecyclerView.Adapter<ThemesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemesAdapter.ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_theme_row))

    override fun onBindViewHolder(holder: ThemesAdapter.ViewHolder, position: Int){
        val theme = themes[position]
        holder.bind(theme)
        holder.itemView.setOnClickListener { view ->
            listener.invoke(theme)
        }
    }

    override fun getItemCount(): Int = themes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(theme : Theme){
            itemView.text_theme_name.text = theme.toString()
        }
    }
}