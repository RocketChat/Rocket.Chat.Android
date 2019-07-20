package chat.rocket.android.thememanager.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.item_theme_row.view.*

class CustomThemesAdapter(private val customThemes: List<Theme>,
                          private val listener: (Int, String) -> Unit,
                          private val colorListener: (Int, String, Boolean) -> Unit) : RecyclerView.Adapter<CustomThemesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_custom_theme_row))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customTheme = customThemes[position]
        holder.bind(customTheme)
        holder.itemView.text_theme_name.setOnClickListener {
            listener.invoke(position, customTheme.name)
        }
        holder.itemView.item_theme_preview_color_accent.setOnClickListener {
            colorListener.invoke(position, "Custom Accent", customTheme.isDark)
        }
        holder.itemView.item_theme_preview_color_primary.setOnClickListener {
            colorListener.invoke(position, "Custom Toolbar", customTheme.isDark)
        }
        holder.itemView.item_theme_preview_color_background.setOnClickListener {
            colorListener.invoke(position, "Custom Background", customTheme.isDark)
        }
    }

    override fun getItemCount(): Int = customThemes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(theme: Theme) {
            theme.setPreviewColors(itemView.context)
            itemView.text_theme_name.text = theme.customName

            val drawableBackground = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()
            val drawableAccent = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()
            val drawablePrimary = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()

            DrawableHelper.tintDrawable(drawableBackground, itemView.context, theme.colorPreviewBackground)
            DrawableHelper.tintDrawable(drawableAccent, itemView.context, theme.colorPreviewAccent)
            DrawableHelper.tintDrawable(drawablePrimary, itemView.context, theme.colorPreviewPrimary)

            itemView.item_theme_preview_color_background.setImageDrawable(drawableBackground)
            itemView.item_theme_preview_color_accent.setImageDrawable(drawableAccent)
            itemView.item_theme_preview_color_primary.setImageDrawable(drawablePrimary)
        }
    }
}