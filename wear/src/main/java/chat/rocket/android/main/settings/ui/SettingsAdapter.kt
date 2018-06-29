package chat.rocket.android.main.settings.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import kotlinx.android.synthetic.main.item_settings.view.*

class SettingsAdapter(
    private val parentContext: Context,
    private val listener: (Int) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder =
        SettingsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_settings,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return 1;
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) = holder.bind(position)

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            with(itemView) {
                setOnClickListener{
                    listener(position)
                }
                when (position) {
                    0 -> item_title.text = parentContext.getString(R.string.settings_item_account)
                }
            }
        }
    }
}