package chat.rocket.android.suggestions.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.transition.Slide
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.suggestions.R
import chat.rocket.android.suggestions.model.SuggestionModel
import chat.rocket.android.suggestions.ui.SuggestionsAdapter.Companion.CONSTRAINT_BOUND_TO_START
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

//  This is a special index that means we're not at an autocompleting state.
private const val NO_STATE_INDEX = 0

class SuggestionsView : FrameLayout, TextWatcher {
    private val recyclerView: RecyclerView
    // Maps tokens to their respective adapters.
    private val adaptersByToken = hashMapOf<String, SuggestionsAdapter<out BaseSuggestionViewHolder>>()
    private val externalProvidersByToken = hashMapOf<String, ((query: String) -> Unit)>()
    private val localProvidersByToken = hashMapOf<String, HashMap<String, List<SuggestionModel>>>()
    private var editor: WeakReference<EditText>? = null
    private var completionOffset = AtomicInteger(NO_STATE_INDEX)
    private var maxHeight: Int = 0

    companion object {
        private val SLIDE_TRANSITION = Slide(Gravity.BOTTOM).setDuration(200)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr, 0) {
        recyclerView = RecyclerView(context)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(TopItemDecoration(context, R.drawable.suggestions_menu_decorator))
        recyclerView.layoutManager = layoutManager
        recyclerView.visibility = View.GONE
        addView(recyclerView)
    }

    override fun afterTextChanged(s: Editable) {
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // If we have a deletion.
        if (after == 0) {
            val deleted = s.subSequence(start, start + count).toString()
            if (adaptersByToken.containsKey(deleted) && completionOffset.get() > NO_STATE_INDEX) {
                // We have removed the '@', '#' or any other action token so halt completion.
                cancelSuggestions(true)
            }
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // If we don't have any adapter bound to any token bail out.
        if (adaptersByToken.isEmpty()) return

        if (editor?.get() != null && editor?.get()?.selectionStart ?: 0 < completionOffset.get()) {
            completionOffset.set(NO_STATE_INDEX)
            collapse()
        }

        val new = s.subSequence(start, start + count).toString()
        if (adaptersByToken.containsKey(new)) {
            val constraint = adapter(new).constraint
            if (constraint == CONSTRAINT_BOUND_TO_START && start != 0) {
                return
            }
            swapAdapter(getAdapterForToken(new)!!)
            completionOffset.compareAndSet(NO_STATE_INDEX, start + 1)
            this.editor?.let {
                // Disable keyboard suggestions when autocompleting.
                val editText = it.get()
                if (editText != null) {
                    editText.inputType = editText.inputType or InputType.TYPE_TEXT_VARIATION_FILTER
                    expand()
                }
            }
        }

        if (new.startsWith(" ")) {
            // just halts the completion execution
            cancelSuggestions(true)
            return
        }

        if (completionOffset.get() == NO_STATE_INDEX) {
            return
        }

        measureTimeMillis {
            val prefixEndIndex = this.editor?.get()?.selectionStart ?: NO_STATE_INDEX
            if (prefixEndIndex == NO_STATE_INDEX || prefixEndIndex < completionOffset.get()) return
            val prefix = s.subSequence(completionOffset.get(), this.editor?.get()?.selectionStart
                    ?: completionOffset.get()).toString()
            recyclerView.adapter?.also {
                it as SuggestionsAdapter
                // we need to look up only after the '@'
                measureTimeMillis { it.autocomplete(prefix) }.let { time ->
                    Log.d("SuggestionsView", "autocomplete($prefix) in $time ms")
                }
                val cacheMap = localProvidersByToken[it.token]
                if (cacheMap != null && cacheMap[prefix] != null) {
                    if (it.itemCount == 0) {
                        it.addItems(cacheMap[prefix]!!)
                    }
                } else {
                    // fetch more suggestions from an external source if any
                    externalProvidersByToken[it.token]?.invoke(prefix)
                }
            }
        }.let { Log.d("SuggestionsView", "whole prefix in $it ms") }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maxHeight > 0) {
            val hSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            super.onMeasure(widthMeasureSpec, hSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun swapAdapter(adapter: SuggestionsAdapter<*>): SuggestionsView {
        recyclerView.adapter = adapter
        // Don't override if user has set an item click listener already
        if (!adapter.hasItemClickListener()) {
            setOnItemClickListener(adapter) {
                // set default item click behavior
            }
        }
        return this
    }

    private fun getAdapterForToken(token: String): SuggestionsAdapter<*>? = adaptersByToken.get(token)

    fun anchorTo(editText: EditText): SuggestionsView {
        editText.removeTextChangedListener(this)
        editText.addTextChangedListener(this)
        editor = WeakReference(editText)
        return this
    }

    fun addTokenAdapter(adapter: SuggestionsAdapter<*>): SuggestionsView {
        adaptersByToken.getOrPut(adapter.token) { adapter }
        return this
    }

    fun addItems(token: String, list: List<SuggestionModel>): SuggestionsView {
        if (list.isNotEmpty()) {
            val adapter = adapter(token)
            localProvidersByToken.getOrPut(token) { hashMapOf() }.put(adapter.term(), list)
            if (completionOffset.get() > NO_STATE_INDEX && adapter.itemCount == 0) expand()
            adapter.addItems(list)
        }
        return this
    }

    fun setMaximumHeight(height: Int): SuggestionsView {
        check(height > 0)
        this.maxHeight = height
        requestLayout()
        return this
    }

    fun setOnItemClickListener(tokenAdapter: SuggestionsAdapter<*>,
                               clickListener: (item: SuggestionModel) -> Unit): SuggestionsView {
        tokenAdapter.setOnClickListener(object : SuggestionsAdapter.ItemClickListener {
            override fun onClick(item: SuggestionModel) {
                insertSuggestionOnEditor(item)
                clickListener.invoke(item)
                cancelSuggestions(true)
                collapse()
            }
        })
        return this
    }

    fun addSuggestionProviderAction(token: String, provider: (query: String) -> Unit): SuggestionsView {
        if (adaptersByToken[token] == null) {
            throw IllegalStateException("token \"$token\" suggestion provider added without adapter")
        }
        externalProvidersByToken.getOrPut(token, { provider })
        return this
    }

    private fun adapter(token: String): SuggestionsAdapter<*> {
        return adaptersByToken[token]
            ?: throw IllegalStateException("no adapter binds to token \"$token\"")
    }

    private fun cancelSuggestions(haltCompletion: Boolean) {
        // Reset completion start index only if we've deleted the token that triggered completion or
        // we finished the completion process.
        if (haltCompletion) {
            completionOffset.set(NO_STATE_INDEX)
        }
        collapse()
    }

    private fun insertSuggestionOnEditor(item: SuggestionModel) {
        editor?.get()?.let {
            val suggestionText = item.text
            it.text.replace(completionOffset.get(), it.selectionStart, "$suggestionText ")
        }
    }

    private fun collapse() {
        TransitionManager.beginDelayedTransition(this, SLIDE_TRANSITION)
        recyclerView.visibility = View.GONE
    }

    private fun expand() {
        TransitionManager.beginDelayedTransition(this, SLIDE_TRANSITION)
        recyclerView.visibility = View.VISIBLE
    }

    private class TopItemDecoration() : RecyclerView.ItemDecoration() {
        private lateinit var divider: Drawable
        private val padding = Rect()

        // Custom divider will be used.
        constructor(context: Context, @DrawableRes drawableResId: Int) : this() {
            val customDrawable = ContextCompat.getDrawable(context, drawableResId)
            if (customDrawable != null) {
                divider = customDrawable
            }
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val left = parent.paddingLeft
            val right = (parent.width - parent.paddingRight)

            val parentParams = parent.layoutParams as FrameLayout.LayoutParams
            val top = parent.top - parentParams.topMargin - parent.paddingTop
            val bottom = top + divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}