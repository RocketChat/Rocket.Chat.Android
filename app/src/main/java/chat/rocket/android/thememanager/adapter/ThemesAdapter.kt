package chat.rocket.android.thememanager.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.item_theme_row.view.*

class ThemesAdapter(private val themes: List<Theme>, private val listener: (Theme) -> Unit) : RecyclerView.Adapter<ThemesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_theme_row))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = themes[position]
        holder.bind(theme)
        holder.itemView.setOnClickListener {
            listener.invoke(theme)
        }
    }

    override fun getItemCount(): Int = themes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(theme: Theme) {
            theme.setPreviewColors(itemView.context)
            itemView.text_theme_name.text = theme.toString()

            val drawablePrimaryText = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()
            val drawableBackground = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()
            val drawableAccent = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()
            val drawablePrimary = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()
            val drawableDescriptiveText = DrawableHelper.getDrawableFromId(R.drawable.theme_preview, itemView.context).mutate()

            DrawableHelper.tintDrawable(drawableBackground, itemView.context, theme.colorPreviewBackground)
            DrawableHelper.tintDrawable(drawablePrimaryText, itemView.context, theme.colorPreviewPrimaryText)
            DrawableHelper.tintDrawable(drawableAccent, itemView.context, theme.colorPreviewAccent)
            DrawableHelper.tintDrawable(drawablePrimary, itemView.context, theme.colorPreviewPrimary)
            DrawableHelper.tintDrawable(drawableDescriptiveText, itemView.context, theme.colorPreviewDescriptiveText)

            itemView.item_theme_preview_primary_text.setImageDrawable(drawablePrimaryText)
            itemView.item_theme_preview_color_background.setImageDrawable(drawableBackground)
            itemView.item_theme_preview_color_accent.setImageDrawable(drawableAccent)
            itemView.item_theme_preview_color_primary.setImageDrawable(drawablePrimary)
            itemView.item_theme_preview_color_descriptive_text.setImageDrawable(drawableDescriptiveText)
        }
    }
}