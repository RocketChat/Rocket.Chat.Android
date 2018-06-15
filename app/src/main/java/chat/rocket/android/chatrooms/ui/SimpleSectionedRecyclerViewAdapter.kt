package chat.rocket.android.chatrooms.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

class SimpleSectionedRecyclerViewAdapter(private val context: Context, private val sectionResourceId: Int, private val textResourceId: Int,
                                         val baseAdapter: ChatRoomsAdapter) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isValid = true
    private val sectionsHeaders = SparseArray<Section>()

    init {
        baseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                isValid = baseAdapter.itemCount > 0
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                isValid = baseAdapter.itemCount > 0
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                isValid = baseAdapter.itemCount > 0
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                isValid = baseAdapter.itemCount > 0
                notifyItemRangeRemoved(positionStart, itemCount)
            }
        })
    }

    class SectionViewHolder(view: View, textResourceId: Int) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById<View>(textResourceId) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, typeView: Int): RecyclerView.ViewHolder {
        return if (typeView == SECTION_TYPE) {
            val view = LayoutInflater.from(context).inflate(sectionResourceId, parent, false)
            SectionViewHolder(view, textResourceId)
        } else {
            baseAdapter.onCreateViewHolder(parent, typeView - 1)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (isSectionHeaderPosition(position)) {
            (viewHolder as SectionViewHolder).title.text = sectionsHeaders.get(position).title
        } else {
            baseAdapter.onBindViewHolder(viewHolder as ChatRoomsAdapter.ViewHolder, sectionedPositionToPosition(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSectionHeaderPosition(position))
            SECTION_TYPE
        else
            baseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1
    }

    class Section(internal var firstPosition: Int, var title: CharSequence) {
        internal var sectionedPosition: Int = 0
    }

    fun setSections(sections: Array<Section>) {
        sectionsHeaders.clear()

        Arrays.sort(sections) { section1, section2 ->
            when {
                section1.firstPosition == section2.firstPosition -> 0
                section1.firstPosition < section2.firstPosition -> -1
                else -> 1
            }
        }

        for ((offset, section) in sections.withIndex()) {
            section.sectionedPosition = section.firstPosition + offset
            sectionsHeaders.append(section.sectionedPosition, section)
        }

        notifyDataSetChanged()
    }

    fun clearSections(){
        sectionsHeaders.clear()
        notifyDataSetChanged()
    }

    fun positionToSectionedPosition(position: Int): Int {
        var offset = 0
        for (i in 0 until sectionsHeaders.size()) {
            if (sectionsHeaders.valueAt(i).firstPosition > position) {
                break
            }
            ++offset
        }
        return position + offset
    }

    private fun sectionedPositionToPosition(sectionedPosition: Int): Int {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION
        }

        var offset = 0
        for (i in 0 until sectionsHeaders.size()) {
            if (sectionsHeaders.valueAt(i).sectionedPosition > sectionedPosition) {
                break
            }
            --offset
        }
        return sectionedPosition + offset
    }

    private fun isSectionHeaderPosition(position: Int): Boolean {
        return sectionsHeaders.get(position) != null
    }

    override fun getItemId(position: Int): Long {
        return when (isSectionHeaderPosition(position)) {
            true -> (Integer.MAX_VALUE - sectionsHeaders.indexOfKey(position)).toLong()
            false -> baseAdapter.getItemId(sectionedPositionToPosition(position))
        }
    }

    override fun getItemCount(): Int {
        return if (isValid) baseAdapter.itemCount + sectionsHeaders.size() else 0
    }

    companion object {
        private const val SECTION_TYPE = 0
    }
}
